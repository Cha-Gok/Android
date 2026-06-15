package com.roro.storage.presentation.home

import androidx.compose.material3.TimeInput
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.domain.GetSelectedLanguageUseCase
import com.roro.core.domain.SetSelectedLanguageUseCase
import com.roro.core.gemma.GemmaDownloadManager
import com.roro.storage.domain.ObserveRecentVoiceNoteUseCase
import com.roro.storage.domain.ObserveTrashTotalCountUseCase
import com.roro.storage.domain.ObserveUserFoldersUseCase
import com.roro.storage.domain.ObserveVoiceNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // 최근 파일 5개 가져오기
    private val observeRecentVoiceNoteUseCase: ObserveRecentVoiceNoteUseCase,
    // 휴지통 개수 가져오기
    private val observeTrashTotalCountUseCase: ObserveTrashTotalCountUseCase,
    // 개인 폴더 아이템 개수 가져오기
    private val observeUserFoldersUseCase: ObserveUserFoldersUseCase,
    // 녹음 언어 저장용
    private val setSelectedLanguageUseCase: SetSelectedLanguageUseCase,
    // 녹음 언어 읽기용
    private val getSelectedLanguageUseCase: GetSelectedLanguageUseCase,
    // root 파일 가져오기
    private val observeVoiceNoteUseCase: ObserveVoiceNoteUseCase,


    // 설정 관련
    private val gemmaDownloadManager: GemmaDownloadManager

) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val effect = _effect.asSharedFlow()

    init {
//        initialize()
        observeCommonData()
        observeDisplayList()
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


            HomeIntent.ClickSearch -> {
                viewModelScope.launch {
                    _effect.emit(HomeEffect.NavigateToSearch)
                }
            }

            HomeIntent.ClickSetting -> {
                viewModelScope.launch {
                    _effect.emit(HomeEffect.NavigateToSettings)
                }
            }

            is HomeIntent.ClickVoiceNote -> {
                viewModelScope.launch {
                    _effect.emit(HomeEffect.NavigateToVoiceNoteDetail(intent.voiceNoteId))
                }
            }
        }
    }

    private fun initialize() {
        // 1. 최근 기록 5개 관찰
        viewModelScope.launch {
            observeRecentVoiceNoteUseCase().collect { voiceNotes ->
                for (i in voiceNotes) {
                    Timber.d("")
                }
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
            observeTrashTotalCountUseCase().collect { trash ->
                _uiState.update {
                    it.copy(
                        trashCount = trash,
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

    // ✅ 핵심: 리스트 전용 관찰 로직 (딱 한 번만 실행됨)
    private fun observeDisplayList() {
        viewModelScope.launch {
            // selectedFolderType이 바뀔 때마다 flatMapLatest가 이전 구독을 취소하고 새 데이터를 가져옴
            _uiState.map { it.selectedFolderType }
                .distinctUntilChanged() // 타입이 실제로 바뀔 때만 동작
                .flatMapLatest { type ->
                    if (type == DefaultFolderType.RECENT) observeRecentVoiceNoteUseCase()
                    else observeVoiceNoteUseCase()
                }
                .collect { notes ->
                    _uiState.update { it.copy(displayVoiceNotes = notes, isLoading = false) }
                }
        }
    }

    private fun observeCommonData() {
        // 휴지통 개수
        viewModelScope.launch {
            observeTrashTotalCountUseCase().collect { count ->
                _uiState.update { it.copy(trashCount = count) }
            }
        }
        // 기본 폴더 개수
        viewModelScope.launch {
            observeVoiceNoteUseCase().collect { list ->
                _uiState.update { it.copy(defaultFolderCount = list.size) }
            }
        }
        // 개인 폴더 개수
        viewModelScope.launch {
            observeUserFoldersUseCase().collect { list ->
                _uiState.update { it.copy(privateFolderCount = list.size) }
            }
        }
        // 언어 설정
        viewModelScope.launch {
            getSelectedLanguageUseCase().collect { lang ->
                _uiState.update { it.copy(selectedTempLanguage = lang) }
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
                            displayVoiceNotes = note,
                            defaultFolderCount = note.size,
                            selectedFolderType = DefaultFolderType.DEFAULT,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }


    fun isModelDownloaded(): Boolean {
        val result = gemmaDownloadManager.isModelDownloaded()
        Timber.tag("HomeVM").d("🤖 모델 다운로드 여부: $result")
        return result
    }

}