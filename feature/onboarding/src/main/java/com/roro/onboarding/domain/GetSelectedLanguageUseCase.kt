package com.roro.onboarding.domain

import com.roro.core.datastore.Language
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSelectedLanguageUseCase @Inject constructor(
    private val repository: OnBoardingRepository
) {
    operator fun invoke(): Flow<Language> {
        return repository.getSelectedLanguage()
    }
}