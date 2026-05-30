package com.roro.storage.presentation.filelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.domain.model.SortType
import com.roro.core.util.toUUIDOrNull
import com.roro.storage.domain.CreateUserFolderUseCase
import com.roro.storage.domain.MoveToFolderUseCase
import com.roro.storage.domain.MoveToTrashVoiceNotesUseCase
import com.roro.storage.domain.ObserveFolders
import com.roro.storage.domain.ObserveVoiceNotesInFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FileListViewModel @Inject constructor(
    private val observeVoiceNotesInFolderUseCase: ObserveVoiceNotesInFolderUseCase,
    private val observeFolders: ObserveFolders,
    private val createUserFolderUseCase: CreateUserFolderUseCase,
    private val moveToTrashVoiceNotesUseCase: MoveToTrashVoiceNotesUseCase,
    private val moveToFolderUseCase: MoveToFolderUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FileListUiState(isLoading = true))
    val uiState: StateFlow<FileListUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<FileListEffect>(
        replay = 0, extraBufferCapacity = 1
    )
    val effect = _effect.asSharedFlow()

    private val INVALID_CHARS_REGEX = Regex("""[/\\:*?"<>|]""")

    fun onIntent(intent: FileListIntent) {
        when (intent) {
            is FileListIntent.Initialize -> {
                _uiState.update { it.copy(folderId = intent.folderId, folderName = intent.folderName) }
                fetchVoiceNoteList()
                fetchFolderList()
            }

            // 1. 뒤로가기 로직 (핵심)
            FileListIntent.ClickBack -> {
                val state = _uiState.value
                when {
                    // 메뉴가 열려있으면 메뉴부터 닫음
                    state.isMenuExpanded -> _uiState.update { it.copy(isMenuExpanded = false) }
                    // 바텀시트가 열려있으면 바텀시트 닫음
                    state.isBottomSheet -> _uiState.update { it.copy(isBottomSheet = false) }
                    // 편집 모드이면 편집 모드 해제
                    state.isSelectMode -> _uiState.update { it.copy(isSelectMode = false, selectedIds = emptySet()) }
                    // 아무것도 해당 안 되면 화면 나감
                    else -> {
                        viewModelScope.launch { _effect.emit(FileListEffect.NavigateBack) }
                    }
                }
            }

            // 2. 더보기 메뉴 제어
            is FileListIntent.ShowMoreMenu -> {
                _uiState.update { it.copy(isMenuExpanded = intent.isShow) }
            }

            // 3. 선택 모드 제어
            FileListIntent.EnterSelectionMode -> {
                _uiState.update { it.copy(isSelectMode = true, isMenuExpanded = false) }
            }

            FileListIntent.ExitSelectionMode -> {
                _uiState.update { it.copy(isSelectMode = false, selectedIds = emptySet()) }
            }

            // 4. 아이템 선택 토글
            is FileListIntent.ToggleSelectItem -> {
                _uiState.update { state ->
                    val newSelection = if (state.selectedIds.contains(intent.id)) {
                        state.selectedIds - intent.id
                    } else {
                        state.selectedIds + intent.id
                    }
                    state.copy(selectedIds = newSelection)
                }
            }

            // 5. 전체 선택 토글
            FileListIntent.ToggleSelectAll -> {
                _uiState.update { state ->
                    // 1. String인 it.id를 UUID로 변환하여 Set<UUID>를 만듭니다.
                    val allIds: Set<UUID> = state.item.mapNotNull { it.id.toUUIDOrNull() }.toSet()

                    // 2. 이제 두 변수 모두 Set<UUID> 타입이므로 비교가 가능합니다.
                    val isAllSelected = state.selectedIds.size == allIds.size && allIds.isNotEmpty()

                    // 3. 타입을 명시적으로 지정하여 컴파일러 에러를 방지합니다.
                    val nextSelection: Set<UUID> = if (isAllSelected) {
                        emptySet()
                    } else {
                        allIds
                    }

                    state.copy(
                        selectedIds = nextSelection, isSelectMode = true, isMenuExpanded = false
                    )
                }
            }

            // 6. 바텀 시트 및 모드 변경
            is FileListIntent.ShowBottomSheet -> {
                _uiState.update { it.copy(isBottomSheet = intent.isShow, sheetMode = FileListSheetMode.FOLDER_LIST) }
            }

            is FileListIntent.ChangeSheetMode -> {
                _uiState.update { it.copy(sheetMode = intent.mode) }
            }

            is FileListIntent.UpdateNewFolderName -> {
                val filteredName = intent.name.replace(INVALID_CHARS_REGEX, "")
                _uiState.update { it.copy(createFolderName = filteredName, errorMessage = null) }
            }

            // 7. 다이얼로그 제어
            is FileListIntent.ShowDeleteDialog -> {
                _uiState.update { it.copy(showDeleteDialog = intent.isShow) }
            }

            is FileListIntent.ShowCreateFolderDialog -> {
                _uiState.update { it.copy(showCreateFolderDialog = intent.isShow) }
            }

            // 나머지 실제 동작들 (UseCases 연결 필요)
            FileListIntent.ConfirmDelete -> {
                removeVoiceNote()
            }

            FileListIntent.ConfirmMove -> {
                moveToFolder()
            }

            is FileListIntent.ConfirmCreateFolder -> {
                createFolder()
            }

            is FileListIntent.ChangeSort -> {
                sortToVoiceNote(intent.sortType)
            }

            FileListIntent.ClickSearch -> {
                viewModelScope.launch { _effect.emit(FileListEffect.NavigateToSearch) }
            }

            FileListIntent.FetchFolderList -> {
                fetchFolderList()
            }

            is FileListIntent.FetchVoiceNoteList -> {
                fetchVoiceNoteList()
            }

            is FileListIntent.SelectTargetFolder -> {
                _uiState.update { it.copy(selectedFolder = intent.folder) }
                Timber.d("선택 된 폴더: ${intent.folder}")
            }
        }
    }

    private fun sortToVoiceNote(sortType: SortType) {
        _uiState.value.item.forEach {
            Timber.d("전 title = ${it.title} createdAt = ${it.createdAt} updatedAt = ${it.updatedAt}")
        }
        _uiState.update { state ->
            val sortedList = when (sortType) {
                SortType.CREATED_AT -> state.item.sortedByDescending { it.createdAt }
                SortType.UPDATED_AT -> state.item.sortedByDescending { it.updatedAt }
            }.toList()

            Timber.d("정렬 실행 [$sortType] 완료: ${sortedList.joinToString { it.title }}")

            state.copy(
                item = sortedList, selectedSortType = sortType, isMenuExpanded = false
            )
        }
        _uiState.value.item.forEach {
            Timber.d("후 title = ${it.title} createdAt = ${it.createdAt} updatedAt = ${it.updatedAt}")
        }
    }

    private fun moveToFolder() {
        viewModelScope.launch {
            val selectedIds = uiState.value.selectedIds.toList()
            val selectedFolderId = uiState.value.selectedFolder?.id
            if (selectedIds.isEmpty() || selectedFolderId == null) return@launch

            try {
                moveToFolderUseCase(selectedIds, selectedFolderId)
                _uiState.update { it.copy(showCreateFolderDialog = false, isSelectMode = false, selectedIds = emptySet(), errorMessage = null) }
                _effect.emit(FileListEffect.ShowToast("${selectedIds.size}개의 파일이 이동되었습니다.."))
            } catch (e: Exception) {
                Timber.e(e, "파일 이동 중 에러 발생")
                _effect.emit(FileListEffect.ShowToast("이동에 실패했습니다. 다시 시도해주세요"))
            }
        }
    }

    private fun removeVoiceNote() {
        viewModelScope.launch {
            val selectedIds = uiState.value.selectedIds.toList()

            if (selectedIds.isEmpty()) return@launch

            try {
                moveToTrashVoiceNotesUseCase(selectedIds)
                _uiState.update { it.copy(showDeleteDialog = false, isSelectMode = false, selectedIds = emptySet(), errorMessage = null) }
                _effect.emit(FileListEffect.ShowToast("${selectedIds.size}개의 파일이 삭제되었습니다."))
            } catch (e: Exception) {
                Timber.e(e, "파일 삭제 중 에러 발생")
                _effect.emit(FileListEffect.ShowToast("삭제에 실패했습니다. 다시 시도해주세요"))
            }
        }
    }

    private fun createFolder() {
        val folderNameInput = _uiState.value.createFolderName.trim()

        if (folderNameInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "이름을 입력해주세요") }
            return
        }

        viewModelScope.launch {
            try {
                if (createUserFolderUseCase(folderNameInput)) {
                    Timber.d("폴더 생성 성공")

                    // 생성 성공 후 입력창 초기화 및 다이얼로그 닫기
                    _uiState.update {
                        it.copy(
                            createFolderName = "", showCreateFolderDialog = false, sheetMode = FileListSheetMode.FOLDER_LIST, errorMessage = null
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _effect.emit(FileListEffect.ShowToast("같은 폴더의 이름이 있습니다."))
                    }
                }
            } catch (e: Exception) {
                viewModelScope.launch {
                    _effect.emit(FileListEffect.ShowToast("저장에 실패했어요. 다시 시도해주세요"))
                }
                Timber.e(e, "폴더 생성 중 에러 발생: ${e.message}")
            }
        }
    }

    private fun fetchVoiceNoteList() {
        viewModelScope.launch {
            observeVoiceNotesInFolderUseCase(_uiState.value.folderId).collect { voiceNotes ->
                _uiState.update { state ->
                    val sortedItems = when (state.selectedSortType) {
                        SortType.CREATED_AT -> voiceNotes.sortedByDescending { it.createdAt }
                        SortType.UPDATED_AT -> voiceNotes.sortedByDescending { it.updatedAt }
                    }

                    state.copy(
                        item = sortedItems, isLoading = false
                    )
                }
            }
        }
    }

    private fun fetchFolderList() {
        viewModelScope.launch {
            observeFolders().collect { folders ->
                // ✅ 폴더 리스트 로그 출력
                Timber.d("FileListViewModel - fetchFolderList: ${folders.size} folders loaded")
                folders.forEach { folder ->
                    Timber.d("Folder: id=${folder.id}, title=${folder.title}, count=${folder.count}")
                }

                _uiState.update {
                    it.copy(
                        folderList = folders, isLoading = false
                    )
                }
            }
        }
    }
}