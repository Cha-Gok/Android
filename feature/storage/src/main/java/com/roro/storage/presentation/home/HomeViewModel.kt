package com.roro.storage.presentation.home

import androidx.compose.foundation.MutatePriority
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.domain.GetSelectedLanguageUseCase
import com.roro.core.domain.SetSelectedLanguageUseCase
import com.roro.storage.domain.ObserveRecentVoiceNoteUseCase
import com.roro.storage.domain.ObserveTrashFoldersUseCase
import com.roro.storage.domain.ObserveUserFoldersUseCase
import com.roro.storage.domain.ObserveVoiceNoteUseCase
import com.roro.storage.domain.ObserveVoiceNotesByNoneNullFolderUseCase
import com.roro.storage.presentation.filelist.FileListIntent
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
class HomeViewModel @Inject constructor(
    // 최근 파일 5개 가져오기
    private val observeRecentVoiceNoteUseCase: ObserveRecentVoiceNoteUseCase,
    // 기본 폴더 아이템 가져오기
    private val observeVoiceNotesByNoneNullFolderUseCase: ObserveVoiceNotesByNoneNullFolderUseCase,
    // 휴지통 개수 가져오기
    private val observeTrashFoldersUseCase: ObserveTrashFoldersUseCase,
    // 개인 폴더 아이템 개수 가져오기
    private val observeUserFoldersUseCase: ObserveUserFoldersUseCase,
    // 녹음 언어 저장용
    private val setSelectedLanguageUseCase: SetSelectedLanguageUseCase,
    // 녹음 언어 읽기용
    private val getSelectedLanguageUseCase: GetSelectedLanguageUseCase,
    // root 파일 가져오기
    private val observeVoiceNoteUseCase: ObserveVoiceNoteUseCase

) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val effect = _effect.asSharedFlow()

    init {
        initialize()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Initialize -> {
                _uiState.update {
                    it.copy(
                        selectedFolderType = DefaultFolderType.RECENT,
                        isLoading = true
                    )
                }
            }

            is HomeIntent.ClickFolderType -> {
                // 현재 선택 된 타입을 업데이트 (UI에서 탭 강조 효과)
                _uiState.update { it.copy(selectedFolderType = intent.type) }

                when (intent.type) {
                    DefaultFolderType.RECENT -> {
                        updateDisplayList(DefaultFolderType.RECENT)
                    }

                    DefaultFolderType.DEFAULT -> {
                        updateDisplayList(DefaultFolderType.DEFAULT)
                    }

                    DefaultFolderType.PRIVATE -> {
                        viewModelScope.launch {
                            _effect.emit(HomeEffect.NavigateToPrivate)
                        }
                    }

                    DefaultFolderType.TRASH -> {
                        viewModelScope.launch {
                            _effect.emit(HomeEffect.NavigateToTrash)
                        }
                    }
                }
            }

            is HomeIntent.FetchVoiceNote -> {
                // 파일 목록 voiceNote 가져오기
            }

            HomeIntent.ClickRecordButton -> {
                viewModelScope.launch {
                    _effect.emit(HomeEffect.NavigateToRecord)
                }
            }

            // 라디오 버튼 클릭 시: 임시 상태 업데이트
            is HomeIntent.SelectLanguageOption -> {
                _uiState.update { it.copy(selectedTempLanguage = intent.language) }
            }

            HomeIntent.ClickSearch -> {
                viewModelScope.launch {
                    _effect.emit(HomeEffect.NavigateToSearch)
                }
            }

            HomeIntent.ClickSetting -> {
                _uiState.update {
                    it.copy(
                        isDialog = true,
                        selectedTempLanguage = it.selectedTempLanguage
                    )
                }
            }

            HomeIntent.ConfirmDialog -> {
                viewModelScope.launch {
                    val languageToSave = uiState.value.selectedTempLanguage
                    setSelectedLanguageUseCase(languageToSave)
                    _uiState.update { it.copy(isDialog = false) }
                }
            }

            HomeIntent.DismissDialog -> {
                _uiState.update { it.copy(isDialog = false) }
            }

            HomeIntent.ClickTos -> {
                viewModelScope.launch {
                    _effect.emit(HomeEffect.NavigateToTos)
                }
            }
        }
    }

    private fun initialize() {
        // 1. 최근 기록 5개 관찰
        viewModelScope.launch {
            observeRecentVoiceNoteUseCase().collect { voiceNotes ->
                _uiState.update {
                    it.copy(
                        displayVoiceNotes = voiceNotes,
                        selectedFolderType = DefaultFolderType.RECENT,
                        isLoading = false
                    )
                }
            }
        }

        // 2. 휴지통 아이템 개수 관찰
        viewModelScope.launch {
            observeTrashFoldersUseCase().collect { trash ->
                _uiState.update {
                    it.copy(
                        trashCount = trash.size,
                        isLoading = false
                    )
                }
            }
        }

        // 3. 기본 폴더 아이템 개수 관찰
        viewModelScope.launch {
            observeVoiceNoteUseCase().collect { default ->
                _uiState.update {
                    it.copy(
                        defaultFolderCount = default.size,
                        isLoading = false
                    )
                }
            }
//            observeVoiceNotesByNoneNullFolderUseCase().collect { default ->
//                _uiState.update {
//                    it.copy(
//                        defaultFolderCount = default.size,
//                        isLoading = false
//                    )
//                }
//            }
        }

        // 3. 개인 폴더 아이템 개수 관찰
        viewModelScope.launch {
            observeUserFoldersUseCase().collect { private ->
                _uiState.update {
                    it.copy(
                        privateFolderCount = private.size,
                        isLoading = false
                    )
                }
            }
        }

        // 4. 사용자 녹음 언어 가져오기
        viewModelScope.launch {
            getSelectedLanguageUseCase().collect { language ->
                Timber.d("사용자 녹음 언어 가져오기 $language")
                _uiState.update {
                    it.copy(
                        selectedTempLanguage = language
                    )
                }
            }
        }
    }

    private fun updateDisplayList(type: DefaultFolderType) {
        viewModelScope.launch {
            if (type == DefaultFolderType.RECENT) {
                observeRecentVoiceNoteUseCase().collect { notes ->
                    _uiState.update {
                        it.copy(
                            displayVoiceNotes = notes,
                            selectedFolderType = DefaultFolderType.RECENT,
                            isLoading = false
                        )
                    }
                }
            } else {
                observeVoiceNoteUseCase().collect { note ->
                    _uiState.update {
                        it.copy(
                            item = note,
                            defaultFolderCount = note.size,
                            selectedFolderType = DefaultFolderType.DEFAULT,
                            isLoading = false
                        )
                    }
                }
//                observeVoiceNotesByNoneNullFolderUseCase().collect { notes ->
//                    _uiState.update {
//                        it.copy(
//                            displayVoiceNotes = notes,
//                            defaultFolderCount = notes.size,
//                            selectedFolderType = DefaultFolderType.DEFAULT,
//                            isLoading = false
//                        )
//                    }
//                }
            }
        }
    }

}