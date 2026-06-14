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
    private var keywords: List<String> = emptyList()

    fun init(summaryText: String, sttText: String, keywords: List<String>) {
        this.summaryText = summaryText
        this.sttText = sttText
        this.keywords = keywords
    }

    fun onQueryChange(query: String, search: Boolean = true) {
        _uiState.value = _uiState.value.copy(
            query = query,
            summaryMatches = if (search && query.isNotBlank()) searchInSummary(query) else emptyList(),
            scriptMatches = if (search && query.isNotBlank()) searchInScript(query) else emptyList(),
            currentMatchIndex = 0
        )
    }

    fun onSearch() {
        val query = _uiState.value.query
        if (query.isBlank()) return
        _uiState.value = _uiState.value.copy(
            summaryMatches = searchInSummary(query),
            scriptMatches = searchInScript(query),
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

        // 핵심 포인트 (segmentIndex 0~2)
        val keyPoints = summaryText.split("*").map { it.trim() }.filter { it.isNotBlank() }.take(3)
        keyPoints.forEachIndexed { pointIndex, point ->
            val lowerPoint = point.lowercase()
            var searchFrom = 0
            while (true) {
                val idx = lowerPoint.indexOf(lowerQuery, searchFrom)
                if (idx < 0) break
                results.add(SearchMatch(
                    segmentIndex = pointIndex,       // 0, 1, 2
                    startTimeMs = 0,
                    text = point,
                    matchStart = idx,
                    matchEnd = idx + query.length
                ))
                searchFrom = idx + 1
            }
        }

        // 키워드 (segmentIndex 100 + keywordIndex로 구분)
        keywords.forEachIndexed { kwIndex, kw ->
            val lowerKw = kw.lowercase()
            val idx = lowerKw.indexOf(lowerQuery)
            if (idx >= 0) {
                results.add(SearchMatch(
                    segmentIndex = 100 + kwIndex,   // 100번대 = 키워드
                    startTimeMs = 0,
                    text = kw,
                    matchStart = idx,
                    matchEnd = idx + query.length
                ))
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