package com.roro.recorder.presentation.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class SearchMatch(
    val segmentIndex: Int,       // 세그먼트 인덱스
    val startTimeMs: Long,       // 세그먼트 시작 시간
    val text: String,            // 세그먼트 전체 텍스트
    val matchStart: Int,         // 매칭 시작 인덱스
    val matchEnd: Int            // 매칭 끝 인덱스
)

data class SearchUiState(
    val query: String = "",
    val summaryMatches: List<SearchMatch> = emptyList(),
    val scriptMatches: List<SearchMatch> = emptyList(),
    val currentMatchIndex: Int = 0,  // 현재 포커스된 결과 (스크립트 탭 기준)
    val selectedTab: Int = 0         // 0: 요약, 1: 스크립트
)

@HiltViewModel
class SearchResultViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var summaryText = ""
    private var sttText = ""

    fun init(summaryText: String, sttText: String) {
        this.summaryText = summaryText
        this.sttText = sttText
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            summaryMatches = if (query.isBlank()) emptyList() else searchInSummary(query),
            scriptMatches = if (query.isBlank()) emptyList() else searchInScript(query),
            currentMatchIndex = 0
        )
    }

    fun onTabSelected(index: Int) {
        _uiState.value = _uiState.value.copy(
            selectedTab = index,
            currentMatchIndex = 0
        )
    }

    fun nextMatch() {
        val matches = currentMatches()
        if (matches.isEmpty()) return
        val next = (_uiState.value.currentMatchIndex + 1) % matches.size
        _uiState.value = _uiState.value.copy(currentMatchIndex = next)
    }

    fun prevMatch() {
        val matches = currentMatches()
        if (matches.isEmpty()) return
        val prev = (_uiState.value.currentMatchIndex - 1 + matches.size) % matches.size
        _uiState.value = _uiState.value.copy(currentMatchIndex = prev)
    }

    private fun currentMatches() = if (_uiState.value.selectedTab == 0)
        _uiState.value.summaryMatches else _uiState.value.scriptMatches

    // ── 요약 텍스트에서 검색 ──────────────────────────────────────────────────

    private fun searchInSummary(query: String): List<SearchMatch> {
        val results = mutableListOf<SearchMatch>()
        val lowerQuery = query.lowercase()

        // ✅ * 기준으로 포인트 나눠서 각각 검색
        val keyPoints = summaryText
            .split("*")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(3)

        keyPoints.forEachIndexed { pointIndex, point ->
            val lowerPoint = point.lowercase()
            var searchFrom = 0
            while (true) {
                val idx = lowerPoint.indexOf(lowerQuery, searchFrom)
                if (idx < 0) break
                results.add(
                    SearchMatch(
                        segmentIndex = pointIndex,  // ✅ 포인트 인덱스
                        startTimeMs = 0,
                        text = point,               // ✅ 포인트 텍스트만
                        matchStart = idx,
                        matchEnd = idx + query.length
                    )
                )
                searchFrom = idx + 1
            }
        }
        return results
    }

    // ── 스크립트 세그먼트에서 검색 ────────────────────────────────────────────

    private fun searchInScript(query: String): List<SearchMatch> {
        val results = mutableListOf<SearchMatch>()
        val lowerQuery = query.lowercase()
        val segments = sttText.split("\n").filter { it.isNotBlank() }

        segments.forEachIndexed { segIndex, segText ->
            val startTimeMs = segIndex * 30000L
            val lowerSeg = segText.lowercase()
            var searchFrom = 0
            while (true) {
                val idx = lowerSeg.indexOf(lowerQuery, searchFrom)
                if (idx < 0) break
                results.add(
                    SearchMatch(
                        segmentIndex = segIndex,
                        startTimeMs = startTimeMs,
                        text = segText,
                        matchStart = idx,
                        matchEnd = idx + query.length
                    )
                )
                searchFrom = idx + 1
            }
        }
        return results
    }
}