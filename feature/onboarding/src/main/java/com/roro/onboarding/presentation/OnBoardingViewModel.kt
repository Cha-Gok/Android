package com.roro.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.gemma.GemmaDownloadState
import com.roro.core.datastore.Language
import com.roro.core.domain.GetSelectedLanguageUseCase
import com.roro.core.domain.SetSelectedLanguageUseCase
import com.roro.core.gemma.DeviceSupportResult
import com.roro.onboarding.domain.DownloadModelsUseCase
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
import java.util.Locale
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

            // PagerChanged - 페이지 3 진입 시 자동 체크 시작
            is OnboardingIntent.PagerChanged -> {
                val previousPage = uiState.value.currentPage
                val wasChecked = uiState.value.isEnvironmentChecked  // update 전에 먼저 읽어
                _uiState.update { it.copy(currentPage = intent.index) }

                Timber.d("PagerChanged: $previousPage -> ${intent.index}, isEnvironmentChecked=$wasChecked")

            }

            // ClickNext
            OnboardingIntent.ClickNext -> {
                val currentPage = uiState.value.currentPage
                val state = uiState.value

                when {
                    currentPage == 2 -> emitEffect(OnboardingEffect.RequestAudioPermission)
                    currentPage == 3 && !state.isEnvironmentChecked && !state.isCheckingEnvironment -> {
                        checkEnvironment()  // 다운로드 버튼 클릭 시 환경 확인 시작
                    }
                    currentPage == 3 && state.isEnvironmentChecked && !state.isDownloadStarted -> {
                        startModelDownload()  // 환경 확인 완료 후 다운로드 시작
                    }
                    currentPage == 3 && state.modelDownloadState.gemma == DownloadItemState.Done -> {
                        emitEffect(OnboardingEffect.ScrollToPage(4))
                    }
                    else -> emitEffect(OnboardingEffect.ScrollToPage(currentPage + 1))
                }
            }

            OnboardingIntent.ClickBack -> {
                val prevPage = uiState.value.currentPage - 1
                Timber.d("Onboarding onIntent: ClickBack, prevPage=$prevPage")
                if (prevPage >= 0) emitEffect(OnboardingEffect.ScrollToPage(prevPage))
            }

            OnboardingIntent.ClickSkip -> {
                val currentPage = uiState.value.currentPage
                Timber.d("Onboarding onIntent: ClickSkip, currentPage=$currentPage")

                when (currentPage) {
                    3 -> emitEffect(OnboardingEffect.ScrollToPage(4))
                    else -> emitEffect(OnboardingEffect.ScrollToPage(2))
                }
            }

            OnboardingIntent.ClickPermissionRequest -> {
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

    // checkEnvironment - 자동 실행용 (버튼 없이)
    private fun checkEnvironment() {

        Timber.d("isCheckingEnvironment=${uiState.value.isCheckingEnvironment}")
        Timber.d("downloadState=${uiState.value.modelDownloadState}")

        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("checkEnvironment 시작")
            _uiState.update {
                it.copy(
                    isCheckingEnvironment = true,
                    modelDownloadState = ModelDownloadState(isChecking = true)
                )
            }
            Timber.d("isCheckingEnvironment = ${uiState.value.isCheckingEnvironment}")

            delay(3000) // 테스트용

            val result = downloadModelsUseCase.checkDeviceSupport()
            val supported = result is DeviceSupportResult.Supported

            _uiState.update {
                it.copy(
                    isCheckingEnvironment = false,
                    modelDownloadState = it.modelDownloadState.copy(
                        isChecking = false,
                        gemma = if (supported)
                            DownloadItemState.Required
                        else
                            DownloadItemState.Unavailable
                    )
                )
            }


            if (!supported) {
                when (result) {
                    is DeviceSupportResult.UnsupportedCpu ->
                        emitEffect(OnboardingEffect.ShowToast("ARM64 미지원 기기입니다."))
                    is DeviceSupportResult.InsufficientRam ->
                        emitEffect(OnboardingEffect.ShowToast("RAM 부족 (현재 ${String.format(Locale.getDefault(), "%.1f", result.actualGb)}GB)"))
                    else -> {}
                }
                return@launch
            }

            // 체크 완료 표시 800ms 후 Idle로 복귀
            delay(800)

            _uiState.update {
                it.copy(
                    isEnvironmentChecked = true
                )
            }
        }
    }

    // 환경 체크 → 완료 시 자동으로 다운로드 시작
    private fun checkAndDownload() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. 체크 시작
            _uiState.update {
                it.copy(
                    modelDownloadState = it.modelDownloadState.copy(isChecking = true)
                )
            }

            // 2. 환경 체크
            val result = downloadModelsUseCase.checkDeviceSupport()

            val gemmaState = when (result) {
                is DeviceSupportResult.Supported -> DownloadItemState.Done
                is DeviceSupportResult.UnsupportedCpu -> DownloadItemState.Unavailable
                is DeviceSupportResult.InsufficientRam -> DownloadItemState.Unavailable
            }

            // 3. 체크 완료 → 체크마크 표시
            _uiState.update {
                it.copy(
                    modelDownloadState = it.modelDownloadState.copy(
                        isChecking = false,
                        gemma = gemmaState
                    )
                )
            }

            // 4. 미지원 토스트
            when (result) {
                is DeviceSupportResult.UnsupportedCpu ->
                    emitEffect(OnboardingEffect.ShowToast("ARM64 미지원 기기입니다."))
                is DeviceSupportResult.InsufficientRam ->
                    emitEffect(OnboardingEffect.ShowToast("RAM이 부족합니다. (현재 ${String.format("%.1f", result.actualGb)}GB)"))
                else -> {}
            }


            // 5. 체크마크 잠깐 보여주고 → Gemma-4로 복귀
            delay(800)

            _uiState.update {
                it.copy(
                    isEnvironmentChecked = true
                )
            }

        }
    }

    private fun startModelDownload() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(

                    modelDownloadState = it.modelDownloadState.copy(
                        gemma = DownloadItemState.Downloading
                    )
                )
            }

            downloadModelsUseCase.downloadGemma().collectLatest { state ->
                when (state) {
                    is GemmaDownloadState.Downloading -> {
                        _uiState.update {
                            it.copy(
                                modelDownloadState = it.modelDownloadState.copy(
                                    progress = state.progress
                                )
                            )
                        }
                    }

                    is GemmaDownloadState.Completed -> {
                        _uiState.update {
                            it.copy(
                                modelDownloadState = it.modelDownloadState.copy(
                                    gemma = DownloadItemState.Done,
                                    progress = 1f
                                )
                            )
                        }
                    }

                    is GemmaDownloadState.Error.NetworkLost -> {
                        _uiState.update {
                            it.copy(
                                modelDownloadState = it.modelDownloadState.copy(
                                    gemma = DownloadItemState.Failed
                                )
                            )
                        }
                        emitEffect(OnboardingEffect.ShowToast("네트워크 연결이 끊겼습니다."))
                    }

                    is GemmaDownloadState.Error.Unknown -> {
                        _uiState.update {
                            it.copy(
                                modelDownloadState = it.modelDownloadState.copy(
                                    gemma = DownloadItemState.Failed
                                )
                            )
                        }
                        emitEffect(OnboardingEffect.ShowToast("다운로드 오류: ${state.message}"))
                    }

                    else -> {}
                }
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
        viewModelScope.launch {
            if (!granted) {
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
        _effect.tryEmit(effect)
    }
}