package com.roro.chagok.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.chagok.splash.domain.GetOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState(isLoading = true))
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()


    private val _effect = MutableSharedFlow<SplashEffect>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val effect = _effect.asSharedFlow()

    init {
        onIntent(SplashIntent.CheckOnboardingStatus)
    }

    fun onIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.CheckOnboardingStatus -> initialize()
        }
    }

    private fun initialize() {
        viewModelScope.launch {
            // 1. 최소 2초간 스플래시 화면 노출 (로고 인지 시간)
            val delayJob = launch { delay(2000L) }

            // 개발용: 항상 온보딩 보이게 강제
            val isFinishOnboarding = true

            // 2. UseCase를 통해 DataStore에서 상태 읽기
            //val isFinishOnboarding = getOnboardingStatusUseCase().first()

            Timber.d("사용자 온보딩 상태 = $isFinishOnboarding")
            // 3. 최소 지연 시간 대기
            delayJob.join()

            if (isFinishOnboarding) {
                _effect.emit(SplashEffect.NavigationToMain)
            } else {
                _effect.emit(SplashEffect.NavigationToOnboarding)
            }
        }
    }

}