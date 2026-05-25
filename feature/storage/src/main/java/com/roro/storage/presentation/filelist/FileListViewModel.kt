package com.roro.storage.presentation.filelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.util.toUUIDOrNull
import com.roro.storage.domain.CreateUserFolderUseCase
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
    private val createUserFolderUseCase: CreateUserFolderUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FileListUiState(isLoading = true))
    val uiState: StateFlow<FileListUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<FileListEffect>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val effect = _effect.asSharedFlow()

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
                    val allIds: Set<UUID> = state.item.mapNotNull { it.id.toUUIDOrNull() }
                        .toSet()

                    // 2. 이제 두 변수 모두 Set<UUID> 타입이므로 비교가 가능합니다.
                    val isAllSelected = state.selectedIds.size == allIds.size && allIds.isNotEmpty()

                    // 3. 타입을 명시적으로 지정하여 컴파일러 에러를 방지합니다.
                    val nextSelection: Set<UUID> = if (isAllSelected) {
                        emptySet()
                    } else {
                        allIds
                    }

                    state.copy(
                        selectedIds = nextSelection,
                        isSelectMode = true,
                        isMenuExpanded = false
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
                _uiState.update { it.copy(createFolderName = intent.name) }
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
                // TODO: DeleteUseCase 호출
                _uiState.update { it.copy(showDeleteDialog = false, isSelectMode = false, selectedIds = emptySet()) }
            }

            FileListIntent.ConfirmMove -> {
                // TODO: MoveUseCase 호출
                _uiState.update { it.copy(isBottomSheet = false, isSelectMode = false, selectedIds = emptySet()) }
            }

            is FileListIntent.ConfirmCreateFolder -> {
                createFolder()
            }

            is FileListIntent.ChangeSort -> {
                _uiState.update { it.copy(isMenuExpanded = false) }
                // TODO: 정렬 로직 적용
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

    private fun createFolder() {
        val folderNameInput = _uiState.value.createFolderName // 사용자가 입력한 이름

        if (folderNameInput.isBlank()) {
            Timber.e("폴더 생성 실패: 폴더 이름이 비어있습니다.")
            return
        }

        viewModelScope.launch {
            try {
                Timber.d("폴더 생성 시도: name = $folderNameInput")

                // ✅ 수정: folderName -> newFolderName (사용자 입력값)
                createUserFolderUseCase(folderNameInput)

                Timber.d("폴더 생성 성공")

                // 생성 성공 후 입력창 초기화 및 다이얼로그 닫기
                _uiState.update {
                    it.copy(
                        createFolderName = "",
                        showCreateFolderDialog = false,
                        sheetMode = FileListSheetMode.FOLDER_LIST
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "폴더 생성 중 에러 발생: ${e.message}")
            }
        }
    }

    private fun fetchVoiceNoteList() {
        viewModelScope.launch {
            observeVoiceNotesInFolderUseCase(_uiState.value.folderId)
                .collect { voiceNotes ->
                    _uiState.update {
                        it.copy(
                            item = voiceNotes,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun fetchFolderList() {
        viewModelScope.launch {
            observeFolders()
                .collect { folders ->
                    // ✅ 폴더 리스트 로그 출력
                    Timber.d("FileListViewModel - fetchFolderList: ${folders.size} folders loaded")
                    folders.forEach { folder ->
                        Timber.d("Folder: id=${folder.id}, title=${folder.title}, count=${folder.count}")
                    }

                    _uiState.update {
                        it.copy(
                            folderList = folders,
                            isLoading = false
                        )
                    }
                }
        }
    }
}