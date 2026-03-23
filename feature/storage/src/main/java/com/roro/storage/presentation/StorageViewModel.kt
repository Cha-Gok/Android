package com.roro.storage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import com.roro.storage.domain.CreateUserFolderUseCase
import com.roro.storage.domain.CreateVoiceNoteUseCase
import com.roro.storage.domain.MoveToTrashUseCase
import com.roro.storage.domain.MoveToTrashVoiceNotesUseCase
import com.roro.storage.domain.ObserveFolderItemCount
import com.roro.storage.domain.ObserveTrashFoldersUseCase
import com.roro.storage.domain.ObserveUserFoldersUseCase
import com.roro.storage.domain.ObserveVoiceNotesByNoneNullFolderUseCase
import com.roro.storage.domain.ObserveVoiceNotesInFolderUseCase
import com.roro.storage.domain.RestoreFromTrashUseCase
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
    private val restoreFromTrashUserCase: RestoreFromTrashUseCase,
    // 폴더 아이템 개수 가져오기
    private val observeFolderItemCount: ObserveFolderItemCount,
    // 폴더 없는 voiceNote
    private val observeVoiceNotesByNoneNullFolderUseCase: ObserveVoiceNotesByNoneNullFolderUseCase,
    // 폴더 있는 voiceNote
    private val observeVoiceNoteInFolderUseCase: ObserveVoiceNotesInFolderUseCase

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
                restoreFromTrashUserCase(folder)
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

    private fun initialize() {
        viewModelScope.launch {
            observeUserFoldersUseCase()
                .collect { folders ->
                    _userFolders.value = folders
                    _uiState.update {
                        it.copy(
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

        viewModelScope.launch {
            observeVoiceNotesByNoneNullFolderUseCase()
                .collect { voiceNotes ->
                    voiceNotes.forEach { note ->
                        Timber.d("voiceNote -> id=${note.id}, title=${note.title}, folderId=${note.folderId}")
                    }
                    _voiceNoteList.value = voiceNotes
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun observeVoiceNoteByFolder(uuid: UUID) {
        viewModelScope.launch {
            observeVoiceNoteInFolderUseCase(uuid = uuid)
                .collect { voiceNotes ->
                    _voiceNoteFolderList.value = voiceNotes
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }

        }
    }
}