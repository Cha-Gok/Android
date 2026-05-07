package com.roro.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.datastore.Language
import com.roro.core.domain.GetSelectedLanguageUseCase
import com.roro.core.domain.SetSelectedLanguageUseCase
import com.roro.onboarding.domain.DownloadModelsUseCase
import com.roro.onboarding.domain.GetSelectedLanguageUseCase
import com.roro.onboarding.domain.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val setSelectedLanguageUseCase: SetSelectedLanguageUseCase,
    private val getSelectedLanguageUseCase: GetSelectedLanguageUseCase,
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
    private val downloadModelsUseCase: DownloadModelsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<OnboardingEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<OnboardingEffect> = _effect.asSharedFlow()

    init {
        // 앱 실행 시 저장된 언어 설정을 불러옵니다.
        onIntent(OnboardingIntent.Initialize)
    }

    fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.Initialize -> {
                Timber.d("Onboarding onIntent: Initialize")
                initialize()
            }

            is OnboardingIntent.SelectLanguage -> {
                Timber.d("Onboarding onIntent: SelectLanguage = ${intent.language}")
                selectLanguage(intent.language)
            }

            is OnboardingIntent.PagerChanged -> {
                Timber.d("Onboarding onIntent: PagerChanged = ${intent.index}")
                _uiState.update { it.copy(currentPage = intent.index) }
            }

            OnboardingIntent.ClickNext -> {
                val currentPage = uiState.value.currentPage
                val nextPage = currentPage + 1
                Timber.d("Onboarding onIntent: ClickNext, currentPage=$currentPage, nextPage=$nextPage")

                when {
                    currentPage == 2 -> emitEffect(OnboardingEffect.RequestAudioPermission)
                    currentPage == 3 && !uiState.value.isDownloadStarted -> startModelDownload() // 시작하기
                    currentPage == 3 && uiState.value.isDownloadStarted -> emitEffect(OnboardingEffect.ScrollToPage(4)) // 다음
                    else -> emitEffect(OnboardingEffect.ScrollToPage(nextPage))
                }

                // 기존 코드
//                if (currentPage == 2) {
//                    Timber.d("Onboarding requesting audio permission on page 2")
//                    emitEffect(OnboardingEffect.RequestAudioPermission)
//                } else {
//                    emitEffect(OnboardingEffect.ScrollToPage(nextPage))
//                }
            }

            OnboardingIntent.ClickBack -> {
                val prevPage = uiState.value.currentPage - 1
                Timber.d("Onboarding onIntent: ClickBack, currentPage=${uiState.value.currentPage}, prevPage=$prevPage")
                if (prevPage >= 0) {
                    emitEffect(OnboardingEffect.ScrollToPage(prevPage))
                }
            }

            OnboardingIntent.ClickSkip -> {
                Timber.d("Onboarding onIntent: ClickSkip")
                emitEffect(OnboardingEffect.ScrollToPage(2))
            }

            OnboardingIntent.ClickPermissionRequest -> {
                Timber.d("Onboarding onIntent: ClickPermissionRequest, currentPage=${uiState.value.currentPage}, granted=${uiState.value.isPermissionGranted}")
                emitEffect(OnboardingEffect.RequestAudioPermission)
            }

            is OnboardingIntent.PermissionResult -> {
                Timber.d("Onboarding onIntent: PermissionResult = ${intent.granted}")
                handlePermissionResult(intent.granted)
            }

            OnboardingIntent.ClickStart -> {
                Timber.d("Onboarding onIntent: ClickStart")
                completeOnboarding()
            }
        }
    }

    // 다운로드 관련 추가
    private fun startModelDownload() {
        viewModelScope.launch(Dispatchers.IO) {
            launch {
                _uiState.update { it.copy(modelDownloadState = it.modelDownloadState.copy(stt = DownloadItemState.Downloading)) }
                runCatching { downloadModelsUseCase.downloadSTT() }
                    .onSuccess { _uiState.update { it.copy(modelDownloadState = it.modelDownloadState.copy(stt = DownloadItemState.Done)) } }
                    .onFailure { _uiState.update { it.copy(modelDownloadState = it.modelDownloadState.copy(stt = DownloadItemState.Failed)) } }
            }
            launch {
                _uiState.update { it.copy(modelDownloadState = it.modelDownloadState.copy(summarize = DownloadItemState.Downloading)) }
                runCatching { downloadModelsUseCase.downloadSummarize() }
                    .onSuccess { _uiState.update { it.copy(modelDownloadState = it.modelDownloadState.copy(summarize = DownloadItemState.Done)) } }
                    .onFailure { _uiState.update { it.copy(modelDownloadState = it.modelDownloadState.copy(summarize = DownloadItemState.Failed)) } }
            }
            launch {
                _uiState.update { it.copy(modelDownloadState = it.modelDownloadState.copy(translate = DownloadItemState.Downloading)) }
                runCatching { downloadModelsUseCase.downloadTranslate() }
                    .onSuccess { _uiState.update { it.copy(modelDownloadState = it.modelDownloadState.copy(translate = DownloadItemState.Done)) } }
                    .onFailure { _uiState.update { it.copy(modelDownloadState = it.modelDownloadState.copy(translate = DownloadItemState.Failed)) } }
            }
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            getSelectedLanguageUseCase().collectLatest { language ->
                _uiState.update { it.copy(selectedLanguage = language) }
            }
        }
    }

    private fun selectLanguage(language: Language) {
        _uiState.update { it.copy(selectedLanguage = language) }
        viewModelScope.launch {
            setSelectedLanguageUseCase(language)
        }
    }

    private fun handlePermissionResult(granted: Boolean) {
        Timber.d("Onboarding handlePermissionResult called. granted=$granted")
        viewModelScope.launch {
            if (granted) {
                Timber.d("Onboarding permission granted. updatedState=${_uiState.value}")
            } else {
                Timber.w("Onboarding permission denied.")
                emitEffect(OnboardingEffect.ShowToast("일부 기능이 제한될 수 있습니다."))
            }
            _uiState.update { it.copy(isPermissionGranted = granted) }
            delay(100)
            emitEffect(OnboardingEffect.ScrollToPage(3))
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                setOnboardingCompletedUseCase()
            }.onSuccess {
                emitEffect(OnboardingEffect.NavigationToMain)
            }.onFailure {
                emitEffect(OnboardingEffect.ShowToast("설정 저장에 실패했습니다."))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun emitEffect(effect: OnboardingEffect) {
        Timber.d("Onboarding emitEffect: $effect")
        val emitted = _effect.tryEmit(effect)
        Timber.d("Onboarding emitEffect result: emitted=$emitted")
    }
}