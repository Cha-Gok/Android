package com.roro.storage.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.navigation.SearchType
import com.roro.storage.domain.ObserveTrashFolderItemCountUseCase
import com.roro.storage.domain.ObserveTrashFoldersUseCase
import com.roro.storage.domain.ObserveTrashVoiceNotesUseCase
import com.roro.storage.domain.SearchFolderUseCase
import com.roro.storage.domain.SearchTrashFolderUseCase
import com.roro.storage.domain.SearchTrashVoiceNoteUseCase
import com.roro.storage.domain.SearchVoiceNoteUseCase
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
class SearchViewModel @Inject constructor(
    private val observeTrashFoldersUseCase: ObserveTrashFoldersUseCase,
    private val observeFolderItemCount: ObserveTrashFolderItemCountUseCase,
    private val observeTrashVoiceNoteUseCase: ObserveTrashVoiceNotesUseCase,
    private val searchTrashFolderUseCase: SearchTrashFolderUseCase,
    private val searchTrashVoiceNoteUseCase: SearchTrashVoiceNoteUseCase,
    private val searchFolderUseCase: SearchFolderUseCase,
    private val searchVoiceNoteUseCase: SearchVoiceNoteUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState(isLoading = true))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SearchEffect>()
    val effect = _effect.asSharedFlow()


    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.ChangeQuery -> {
                _uiState.update { it.copy(searchQuery = intent.query) }
            }

            is SearchIntent.ClickItem -> {}
            SearchIntent.ClickKeyboardSearch -> {
                performSearch()
            }

            is SearchIntent.InitSearchType -> {
                val type = try {
                    SearchType.valueOf(intent.searchType)
                } catch (e: Exception) {
                    SearchType.HOME
                }
                _uiState.update { it.copy(searchType = type) }
            }

            SearchIntent.ClickClose -> {
                viewModelScope.launch {
                    _effect.emit(SearchEffect.NavigateBack)
                }
            }
        }
    }

    private fun performSearch() {
        val query = _uiState.value.searchQuery
        val type = _uiState.value.searchType

        Timber.d("🔍 [DEBUG] 검색 시도 - 쿼리: '$query', 타입: $type (Name: ${type.name})")

        if (query.isBlank()) {
            Timber.w("⚠️ 쿼리가 비어있어 검색을 중단합니다.")
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                Timber.d("🚀 [DEBUG] Coroutine 시작 - 현재 타입: $type")

                val results = when (type) {
                    SearchType.TRASH -> {
                        Timber.d("📂 [DEBUG] TRASH 검색 진입")
                        val folderRes = searchTrashFolderUseCase(query)
                        val voiceNoteRes = searchTrashVoiceNoteUseCase(query)
                        Timber.d("✅ [DEBUG] TRASH 결과 - 폴더: ${folderRes.size}, 메모: ${voiceNoteRes.size}")
                        (folderRes + voiceNoteRes).sortedByDescending { it.createAt }
                    }

                    SearchType.HOME -> {
                        Timber.d("🏠 [DEBUG] HOME 검색 진입")
                        val folderRes = searchFolderUseCase(query)
                        Timber.d("🏠 [DEBUG] 폴더 검색 완료: ${folderRes.size}개")

                        val voiceNoteRes = searchVoiceNoteUseCase(query)
                        Timber.d("🏠 [DEBUG] 음성메모 검색 완료: ${voiceNoteRes.size}개")

                        voiceNoteRes.forEach {
                            Timber.d("   🎙️ 음성메모 상세: ${it.voiceNoteItem?.title} (폴더: ${it.voiceNoteItem?.folderName})")
                        }
                        (folderRes + voiceNoteRes).sortedByDescending { it.createAt }
                    }

                    SearchType.FOLDER -> {
                        Timber.d("폴더 검색 진입  ")
                        val folderRes = searchFolderUseCase(query)
                        folderRes.sortedByDescending { it.createAt }
                    }

                    else -> {
                        Timber.w("❓ [DEBUG] 알 수 없는 타입입니다: $type (else 문 실행)")
                        emptyList()
                    }
                }

                Timber.d("🎯 [DEBUG] 최종 리스트 업데이트 - 사이즈: ${results.size}")
                _uiState.update {
                    it.copy(
                        itemList = results,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ [DEBUG] 검색 작업 중 예외 발생")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
