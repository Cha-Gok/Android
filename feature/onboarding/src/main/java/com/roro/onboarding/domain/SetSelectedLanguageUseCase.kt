package com.roro.onboarding.domain

import com.roro.core.datastore.Language
import javax.inject.Inject

class SetSelectedLanguageUseCase @Inject constructor(
    private val repository: OnBoardingRepository
) {
    suspend operator fun invoke(language: Language) {
        repository.setSelectedLanguage(language = language)
    }
}