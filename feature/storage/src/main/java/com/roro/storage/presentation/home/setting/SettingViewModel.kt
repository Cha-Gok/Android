package com.roro.storage.presentation.home.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.datastore.Language
import com.roro.core.domain.GetSelectedLanguageUseCase
import com.roro.core.domain.SetSelectedLanguageUseCase
import com.roro.core.gemma.GemmaDownloadManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val gemmaDownloadManager: GemmaDownloadManager,
    private val getSelectedLanguageUseCase: GetSelectedLanguageUseCase,
    private val setSelectedLanguageUseCase: SetSelectedLanguageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SettingEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: SettingIntent) {
        when (intent) {
            SettingIntent.LoadData -> loadData()
            is SettingIntent.SelectLanguage -> selectLanguage(intent.language)
            SettingIntent.ConfirmLanguage -> confirmLanguage()
            SettingIntent.DeleteGemmaModel -> deleteGemmaModel()
            SettingIntent.NavigateBack -> navigateBack()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            getSelectedLanguageUseCase().collect { lang ->
                _uiState.update {
                    it.copy(
                        selectedLanguage = lang,
                        isModelDownloaded = gemmaDownloadManager.isModelDownloaded()
                    )
                }
            }
        }
    }

    private fun selectLanguage(language: Language) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    private fun confirmLanguage() {
        viewModelScope.launch {
            setSelectedLanguageUseCase(_uiState.value.selectedLanguage)
            _effect.emit(SettingEffect.ShowToast("언어가 변경되었습니다."))
        }
    }

    private fun deleteGemmaModel() {
        viewModelScope.launch {
            val success = gemmaDownloadManager.deleteModel()
            _uiState.update { it.copy(isModelDownloaded = false) }
            _effect.emit(
                SettingEffect.ShowToast(
                    if (success) "모델이 삭제되었습니다." else "삭제에 실패했습니다."
                )
            )
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effect.emit(SettingEffect.NavigateBack)
        }
    }
}