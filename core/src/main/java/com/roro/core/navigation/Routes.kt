package com.roro.core.navigation

/**
 * 기능 설명: 네비게이션 Route 정리
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
object Routes {
    // Main 바텀 네비게이션 O
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val STORAGE = "storage"
    const val RECORDER = "record"


    // 바텀 네비게이션 X
    fun splash() = "splash"
    fun onBoarding() = "onboarding"

    const val STORAGE_DETAIL = "storage/detail/{folderId}"
    fun storageDetail(folderId: String) = "storage/detail/$folderId"

    const val RECORD_DETAIL = "recoder/detail/{fileId}"
    fun recorderDetail(fileId: String) = "recoder/detail/$fileId"

    // 녹음 결과 화면 추가
    const val RECORD_RESULT = "record/result/{voiceNoteId}"
    fun recordResult(voiceNoteId: String) = "record/result/$voiceNoteId"

    // 스크립트 편집 화면
    const val SCRIPT_EDIT = "record/script-edit/{voiceNoteId}"
    fun scriptEdit(voiceNoteId: String) = "record/script-edit/$voiceNoteId"

    // 검색 화면
    const val SEARCH = "record/search/{voiceNoteId}"
    fun search(voiceNoteId: String) = "record/search/$voiceNoteId"

}