package com.roro.storage.presentation.filelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class FileListViewModel @Inject constructor(
    private val observeVoiceNotesInFolderUseCase: ObserveVoiceNotesInFolderUseCase
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
            FileListIntent.ClickSearch -> {
                viewModelScope.launch {
                    _effect.emit(FileListEffect.NavigateToSearch)
                }
            }

            FileListIntent.FetchFolderList -> {

            }

            is FileListIntent.Initialize -> {
                _uiState.update { it.copy(folderId = intent.folderId, folderName = intent.folderName) }
                fetchVoiceNoteList()
            }

            is FileListIntent.FetchVoiceNoteList -> {

            }

            is FileListIntent.MoveToTrashVoiceNotes -> {}
            FileListIntent.ClickCloseSelectMode -> {}
            is FileListIntent.ChangeSort -> {}
            FileListIntent.ClickMore -> {}
            FileListIntent.ClickMoveItems -> {}
            FileListIntent.ClickRemove -> {}
            is FileListIntent.ClickSelectItem -> {}
            FileListIntent.ClickSelectMode -> {}
            FileListIntent.DialogCancel -> {}
            FileListIntent.DialogConfirm -> {}
            FileListIntent.DismissMoreMenu -> {}
            FileListIntent.DismissMoveSheet -> {}
            FileListIntent.ToggleSelectAll -> {}
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
//            for (i in _uiState.value.voiceNoteList) {
//                Timber.d("item = ${i.title}")
//            }
        }
    }
}