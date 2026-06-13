package com.roro.storage.presentation.folderlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.domain.model.FolderItem
import com.roro.core.model.Folder
import com.roro.storage.domain.CreateUserFolderUseCase
import com.roro.storage.domain.MoveToTrashUseCase
import com.roro.storage.domain.ObserveFolders
import com.roro.storage.domain.RenameFolderUseCase
import com.roro.storage.presentation.folderlist.PrivateFolderEffect.NavigateFileList
import com.roro.storage.presentation.home.FolderDialogType
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
class PrivateFolderViewModel @Inject constructor(
    private val observeFolders: ObserveFolders,
    private val createUserFolderUseCase: CreateUserFolderUseCase,
    private val renameFolderUseCase: RenameFolderUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrivateFolderUiState(isLoading = true))
    val uiState: StateFlow<PrivateFolderUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<PrivateFolderEffect>(
        replay = 0, extraBufferCapacity = 1
    )
    val effect = _effect.asSharedFlow()


    fun onIntent(intent: PrivateFolderIntent) {
        when (intent) {
            PrivateFolderIntent.Initialize -> initialize()
            PrivateFolderIntent.FetchFolderList -> fetchFolderList()

            // 네비게이션
            PrivateFolderIntent.ClickSearch -> viewModelScope.launch { _effect.emit(PrivateFolderEffect.NavigateSearch) }
            PrivateFolderIntent.ClickBack -> {
                viewModelScope.launch { _effect.emit(PrivateFolderEffect.NavigateBack) }
            }

            is PrivateFolderIntent.ClickPrivateFolder -> {
                viewModelScope.launch {
                    _effect.emit(NavigateFileList(folderId = intent.folderId.id, folderName = intent.folderId.title))
                }
            }

            // 스와이프 상태 관리
            is PrivateFolderIntent.OnFolderSwipe -> {
                _uiState.update { it.copy(swipeFolder = intent.folder) }
            }

            // 다이얼로그 열기
            is PrivateFolderIntent.ShowCreateFolderDialog -> {
                _uiState.update {
                    it.copy(
                        dialogType = FolderDialogType.CREATE,
                        inputFolderName = ""
                    )
                }
            }

            is PrivateFolderIntent.ShowModifyFolderDialog -> {
                _uiState.update {
                    it.copy(
                        dialogType = FolderDialogType.RENAME,
                        selectedFolder = intent.folder,
                        inputFolderName = intent.folder.title,
                        swipeFolder = null
                    )
                }
            }

            // 다이얼로그 입력 및 닫기
            is PrivateFolderIntent.InputFolderName -> {
                _uiState.update { it.copy(inputFolderName = intent.input) }
            }

            PrivateFolderIntent.DismissDialog -> {
                _uiState.update {
                    it.copy(
                        dialogType = null,
                        inputFolderName = "",
                        selectedFolder = null
                    )
                }
            }

            PrivateFolderIntent.ConfirmDialog -> {
                confirmFolderAction()
            }

            // 삭제
            is PrivateFolderIntent.RemoveFolder -> {
                moveToTrash(intent.folder)
            }
        }
    }

    private fun initialize() {
        fetchFolderList()
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

    private fun confirmFolderAction() {
        val state = _uiState.value
        val type = state.dialogType ?: return
        val name = state.inputFolderName
        val selected = state.selectedFolder

        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                when (type) {
                    FolderDialogType.CREATE -> {
                        createUserFolderUseCase(name)
                        _effect.emit(PrivateFolderEffect.ShowToast("폴더를 생성했습니다."))
                    }

                    FolderDialogType.RENAME -> {
                        selected?.let { item ->
                            val folderToUpdate = Folder(
                                id = UUID.fromString(item.id),
                                name = name,
                                updatedAt = System.currentTimeMillis()
                            )
                            renameFolderUseCase(folderToUpdate)
                            _effect.emit(PrivateFolderEffect.ShowToast("이름을 수정했습니다."))
                        }
                    }

                    FolderDialogType.DELETE_SELECTED -> {}
                }
                onIntent(PrivateFolderIntent.DismissDialog)
            } catch (e: Exception) {
                _effect.emit(PrivateFolderEffect.ShowToast("작업에 실패했습니다."))
            }
        }
    }

    private fun moveToTrash(item: FolderItem) {
        viewModelScope.launch {
            moveToTrashUseCase(UUID.fromString(item.id))
            _effect.emit(PrivateFolderEffect.ShowToast("휴지통으로 이동되었어요"))
        }
    }
}