package com.roro.storage.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import com.roro.storage.domain.CreateUserFolderUseCase
import com.roro.storage.domain.CreateVoiceNoteUseCase
import com.roro.storage.domain.MoveToTrashUseCase
import com.roro.storage.domain.MoveToTrashVoiceNotesUseCase
import com.roro.storage.domain.ObserveFolderItemCount
import com.roro.storage.domain.ObserveRecentVoiceNoteUseCase
import com.roro.storage.domain.ObserveTrashFoldersUseCase
import com.roro.storage.domain.ObserveTrashVoiceNotesUseCase
import com.roro.storage.domain.ObserveUserFoldersUseCase
import com.roro.storage.domain.ObserveVoiceNotesByNoneNullFolderUseCase
import com.roro.storage.domain.ObserveVoiceNotesInFolderUseCase
import com.roro.storage.domain.RemoveFolderUseCase
import com.roro.storage.domain.RemoveVoiceNoteUseCase
import com.roro.storage.domain.RenameFolderUseCase
import com.roro.storage.domain.RenameVoiceNoteUseCase
import com.roro.storage.domain.RestoreFolderUseCase
import com.roro.storage.domain.RestoreVoiceNoteUseCase
import com.roro.storage.presentation.StorageEffect
import com.roro.storage.presentation.StorageIntent
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
import kotlin.collections.iterator

@HiltViewModel
class StorageViewModel @Inject constructor(
    // 사용자 폴더 생성
    private val createUserFolderUseCase: CreateUserFolderUseCase,
    // 녹음 파일 생성
    private val createVoiceNoteUserCase: CreateVoiceNoteUseCase,
    // room에서 폴더 가져오기
    private val observeUserFoldersUseCase: ObserveUserFoldersUseCase,
    // 여러 VoiceNotes 휴지통
    private val moveToTrashVoiceNotesUseCase: MoveToTrashVoiceNotesUseCase,
    // 휴지통에서 폴더 가져오기
    private val observeTrashFoldersUserCase: ObserveTrashFoldersUseCase,
    // 폴더 휴지통으로 이동
    private val moveToTrashUseCase: MoveToTrashUseCase,
    // 휴지통 -> 복원
    private val restoreFromTrashUserCase: RestoreFolderUseCase,
    // voiceNote 휴지통 -> 복원
    private val restoreVoiceNoteUserCase: RestoreVoiceNoteUseCase,
    // 폴더 아이템 개수 가져오기
    private val observeFolderItemCount: ObserveFolderItemCount,
    // 폴더 없는 voiceNote
    private val observeVoiceNotesByNoneNullFolderUseCase: ObserveVoiceNotesByNoneNullFolderUseCase,
    // 폴더 있는 voiceNote
    private val observeVoiceNoteInFolderUseCase: ObserveVoiceNotesInFolderUseCase,
    // 휴지통 VoiceNotes 확인
    private val observeTrashVoiceNotesUseCase: ObserveTrashVoiceNotesUseCase,
    // 최근 문서 5개
    private val observeRecentVoiceNoteUseCase: ObserveRecentVoiceNoteUseCase,
    // voiceNote 제거
    private val removeVoiceNoteUseCase: RemoveVoiceNoteUseCase,
    // Folder 제거
    private val removeFolderUseCase: RemoveFolderUseCase,
    // Rename Folder
    private val renameFolderUseCase: RenameFolderUseCase,
    // Rename VoiceNote
    private val renameVoiceNoteUseCase: RenameVoiceNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StorageUiState(isLoading = true))
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<StorageEffect>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val effect = _effect.asSharedFlow()

    private val _userFolders =
        MutableStateFlow<List<Folder>>(emptyList())
    val userFolders: StateFlow<List<Folder>> = _userFolders.asStateFlow()

    private val _trashFolders =
        MutableStateFlow<List<Folder>>(emptyList())
    val trashFolders: StateFlow<List<Folder>> = _trashFolders.asStateFlow()

    private val _folderItemCountMap = MutableStateFlow<Map<UUID?, Int>>(emptyMap())
    val folderItemCountMap: StateFlow<Map<UUID?, Int>> = _folderItemCountMap.asStateFlow()

    private val _voiceNoteList = MutableStateFlow<List<VoiceNote>>(emptyList())
    val voiceNoteList: StateFlow<List<VoiceNote>> = _voiceNoteList.asStateFlow()

    private val _voiceNoteFolderList = MutableStateFlow<List<VoiceNote>>(emptyList())
    val voiceNoteFolderList: StateFlow<List<VoiceNote>> = _voiceNoteFolderList.asStateFlow()

    private val _voiceNoteTrashList = MutableStateFlow<List<VoiceNote>>(emptyList())
    val voiceNoteTrashList: StateFlow<List<VoiceNote>> = _voiceNoteTrashList.asStateFlow()

    private val _voiceNoteRecentList = MutableStateFlow<List<VoiceNote>>(emptyList())
    val voiceNoteRecentList: StateFlow<List<VoiceNote>> = _voiceNoteRecentList.asStateFlow()

    init {
        onIntent(StorageIntent.Initialize)
    }

    fun onIntent(intent: StorageIntent) {
        when (intent) {
            StorageIntent.Initialize -> initialize()
            is StorageIntent.CreateFolder -> createUserFolder(intent.folderName)
            is StorageIntent.MoveToTrash -> moveToTrash(intent.folder)
            is StorageIntent.RestoreFromTrash -> restoreFromTrash(intent.folder)
            is StorageIntent.CreateDummyVoiceNote -> createVoiceNote(intent.folderName)
            is StorageIntent.MoveToTrashVoiceNotes -> moveToTrashVoiceNots(intent.ids)
            StorageIntent.RefreshDefaults -> Unit
            is StorageIntent.RestoreVoiceNote -> restoreVoiceNote(intent.voiceNote)
            is StorageIntent.RemoveVoiceNote -> removeVoiceNote(intent.voiceNote)
            is StorageIntent.RemoveFolder -> removeFolder(intent.folder)
            is StorageIntent.RenameFolder -> renameFolder(intent.folder)
            is StorageIntent.RenameVoiceNote -> renameVoiceNote(intent.voiceNote)
            is StorageIntent.ClickFolderType -> {
                Timber.d("StorageLog: Folder clicked - ${intent.type}")
                val filteredNotes = when (intent.type) {
                    DefaultFolderType.RECENT -> voiceNoteRecentList.value
                    DefaultFolderType.DEFAULT -> voiceNoteList.value
                    DefaultFolderType.PRIVATE -> emptyList<VoiceNote>()
                    DefaultFolderType.TRASH -> voiceNoteTrashList.value
                }
                Timber.d("StorageLog: Filtered list size - ${filteredNotes.size}")

                _uiState.update {
                    it.copy(
                        selectedFolderType = intent.type,
                        voiceNote = filteredNotes
                    )
                }
            }

            is StorageIntent.FetchVoiceNote -> {
//                observeVoiceNoteByFolder(intent.folderId)
            }

            is StorageIntent.SortByCreatedAt -> {
                // 현재 리스트를 생성일 순으로 정렬 (최신순)
                val sortedList = _voiceNoteFolderList.value.sortedByDescending { it.createdAt }
                _voiceNoteFolderList.value = sortedList
                for (i in _voiceNoteFolderList.value) {
                    Timber.d("최신순 = ${i.title}")
                }
            }

            is StorageIntent.SortByUpdatedAt -> {
                // 수정일 필드가 있다면 해당 필드로 정렬
                val sortedList = _voiceNoteFolderList.value.sortedByDescending { it.updatedAt }
                _voiceNoteFolderList.value = sortedList
                for (i in _voiceNoteFolderList.value) {
                    Timber.d("수정순 = ${i.title}")
                }
            }
        }
    }

    private fun createVoiceNote(folderName: String) {
        viewModelScope.launch {
            createVoiceNoteUserCase(folderName)
        }
    }

    private fun createUserFolder(folderName: String) {
        viewModelScope.launch {
            if (folderName.isBlank()) {
                _effect.emit(StorageEffect.ShowToast("폴더 이름을 입력해주세요"))
                return@launch
            }

            runCatching {
                createUserFolderUseCase(folderName)
            }.onSuccess { created ->
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                if (created) {
                    _effect.emit(StorageEffect.ShowToast("폴더가 생성되었습니다."))
                    _effect.emit(StorageEffect.ClearFolderInput)
                } else {
                    _effect.emit(StorageEffect.ShowToast("이미 존재하는 폴더 이름입니다."))
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "폴더 생성 실패") }
                _effect.emit(StorageEffect.ShowToast("사용자 폴더 생성 실패: ${e.message ?: "알 수 없는 오류"}"))
            }
        }
    }

    private fun restoreFromTrash(folder: Folder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                restoreFromTrashUserCase(folder.id)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                _effect.emit(StorageEffect.ShowToast("복원했습니다."))
            }.onFailure { e ->
                Timber.e(e, "moveToTrash failure")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                _effect.emit(StorageEffect.ShowToast("이동 실패: ${e.message ?: "알 수 없는 오류"}"))
            }
        }
    }

    private fun removeFolder(folder: Folder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                removeFolderUseCase(folderId = folder.id)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                _effect.emit(StorageEffect.ShowToast("folder 영구삭제"))
            }.onFailure { e ->
                Timber.e(e, "remove Folder")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                _effect.emit(StorageEffect.ShowToast("삭제 실패: ${e.message ?: "알 수 없는 오류"}"))
            }
        }
    }

    private fun removeVoiceNote(voiceNote: VoiceNote) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                removeVoiceNoteUseCase(voiceNoteId = voiceNote.id)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                _effect.emit(StorageEffect.ShowToast("voiceNote영구삭제"))
            }.onFailure { e ->
                Timber.e(e, "removeVoiceNote")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                _effect.emit(StorageEffect.ShowToast("삭제 실패: ${e.message ?: "알 수 없는 오류"}"))
            }
        }
    }

    private fun restoreVoiceNote(voiceNote: VoiceNote) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                restoreVoiceNoteUserCase(voiceNoteId = voiceNote.id)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                _effect.emit(StorageEffect.ShowToast("복원했습니다."))
            }.onFailure { e ->
                Timber.e(e, "restoreVoiceNote failure")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                _effect.emit(StorageEffect.ShowToast("이동 실패: ${e.message ?: "알 수 없는 오류"}"))
            }
        }
    }

    private fun moveToTrash(folder: Folder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                moveToTrashUseCase(folder)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                _effect.emit(StorageEffect.ShowToast("휴지통으로 이동했어요"))
            }.onFailure { e ->
                Timber.e(e, "moveToTrash failure")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                _effect.emit(StorageEffect.ShowToast("이동 실패: ${e.message ?: "알 수 없는 오류"}"))
            }
        }
    }

    private fun moveToTrashVoiceNots(ids: List<UUID>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                moveToTrashVoiceNotesUseCase(ids)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                _effect.emit(StorageEffect.ShowToast("휴지통으로 이동"))
            }.onFailure { e ->
                Timber.e(e, "moveToTrash failure")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                _effect.emit(StorageEffect.ShowToast("이동 실패: ${e.message ?: "알 수 없는 오류"}"))
            }
        }
    }

    // 폴더 이름 변경
    private fun renameFolder(folder: Folder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                renameFolderUseCase(folder)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                _effect.emit(StorageEffect.ShowToast("폴더 이름 변경"))
            }.onFailure { e ->
                Timber.e(e, "renameFolder failure")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }

                // 에러 메시지에 "already exists" 등이 포함되어 있는지 체크하거나 공통 메시지 출력
                val errorMsg = if (e.message?.contains("exists", ignoreCase = true) == true) {
                    "이미 존재하거나 사용 중인 폴더 이름입니다."
                } else {
                    "이름 변경 실패: ${e.message ?: "알 수 없는 오류"}"
                }
                _effect.emit(StorageEffect.ShowToast(errorMsg))
            }
        }
    }

    private fun renameVoiceNote(voiceNote: VoiceNote) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                renameVoiceNoteUseCase(voiceNote)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                _effect.emit(StorageEffect.ShowToast("voiceNote 이름 변경"))
            }.onFailure { e ->
                Timber.e(e, "moveToTrash failure")
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                _effect.emit(StorageEffect.ShowToast("voiceNote 이름 변경 실패: ${e.message ?: "알 수 없는 오류"}"))
            }
        }
    }

    // 최근 아이템 가져오기
    private fun fetchRecentItem() {
        // 최근 문서
        viewModelScope.launch {
            observeRecentVoiceNoteUseCase().collect { voiceNotes ->
                Timber.d("StorageLog: RecentNote fetch = ${voiceNotes.size}")
                _voiceNoteRecentList.value = voiceNotes

                // 현재 사용자가 '최근 기록' 탭을 보고 있다면 ui 갱신
                if (_uiState.value.selectedFolderType == DefaultFolderType.RECENT) {
                    _uiState.update { it.copy(voiceNote = voiceNotes) }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    // 기본 폴더 아이템 가져오기
    private fun fetchDefaultItems() {
        viewModelScope.launch {
            observeVoiceNotesByNoneNullFolderUseCase().collect { voiceNotes ->
                Timber.d("StorageLog: Default fetch = ${voiceNotes.size}")
                _voiceNoteList.value = voiceNotes

                // 현재 사용자가 '기본 폴더' 탭을 보고 있다면 ui 갱신
                if (_uiState.value.selectedFolderType == DefaultFolderType.DEFAULT) {
                    _uiState.update { it.copy(voiceNote = voiceNotes) }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    // 휴지통 아이템 가져오기
    private fun fetchTrashItems() {
        viewModelScope.launch {
            observeTrashVoiceNotesUseCase().collect { voiceNotes ->
                Timber.d("StorageLog: Trash fetch = ${voiceNotes.size}")
                _voiceNoteTrashList.value = voiceNotes

                // 현재 사용자가 '휴지통' 탭을 보고 있다면 ui 갱신
                if (_uiState.value.selectedFolderType == DefaultFolderType.TRASH) {
                    _uiState.update { it.copy(voiceNote = voiceNotes) }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }


    // 화면 초기 갱신
    private fun initialize() {
        fetchRecentItem()
        fetchDefaultItems()
        fetchTrashItems()
        fetchFolderCount()

        viewModelScope.launch {
            observeUserFoldersUseCase()
                .collect { folders ->
                    _userFolders.value = folders
                    _uiState.update {
                        it.copy(
                            folders = folders,
//                            folderItemCountMap = _folderItemCountMap.value,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
        viewModelScope.launch {
            observeTrashFoldersUserCase()
                .collect { folders ->
                    _trashFolders.value = folders
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
        viewModelScope.launch {
            observeFolderItemCount()
                .collect { item ->
                    _folderItemCountMap.value = item.associate { count ->
                        count.id to count.noteCount
                    }
                }
        }
    }

    fun fetchFolderCount() {
        viewModelScope.launch {
            observeFolderItemCount()
                .collect { item ->
                    val countMap = item.associate { it.id to it.noteCount }
                    _folderItemCountMap.value = countMap
                    for (i in _folderItemCountMap.value) {
                        Timber.d("count = ${i.value}")
                    }
//                    _uiState.update { it.copy(folderItemCountMap = countMap) }
                }
        }
    }

//    fun observeVoiceNoteByFolder(uuid: UUID) {
//        viewModelScope.launch {
//            observeVoiceNoteInFolderUseCase(uuid = uuid)
//                .collect { voiceNotes ->
//                    _voiceNoteFolderList.value = voiceNotes
//                    _uiState.update {
//                        it.copy(
//                            isLoading = false,
//                            errorMessage = null
//                        )
//                    }
//                }
//
//        }
//    }
}