package com.roro.core.navigation

/**
 * 기능 설명: 네비게이션 Route 정리
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
object Routes {
    // Main
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val STORAGE = "storage"
    const val RECORDER = "record"
    const val TRASH = "trash"
    const val TOS = "tos" // 이용약관
    const val SEARCH_TEMP = "search/{searchType}" // 검색


    // 스토리지 화면 구성
    // 폴더 목록
    const val STORAGE_FOLDER = "storage/folder"

    // 폴더 목록 > 파일 목록
    const val STORAGE_FILE = "storage/folder/file/{folderId}/{folderName}"
    fun storageFile(folderId: String, folderName: String) = "storage/folder/file/${folderId}/${folderName}"


    const val RECORD_DETAIL = "recoder/detail/{fileId}"
    fun recorderDetail(fileId: String) = "recoder/detail/$fileId"

    // 녹음 결과 화면 추가
    const val RECORD_RESULT = "record/result/{voiceNoteId}"
    fun recordResult(voiceNoteId: String) = "record/result/$voiceNoteId"

    // 스크립트 편집 화면
    const val SCRIPT_EDIT = "record/script-edit/{voiceNoteId}"
    fun scriptEdit(voiceNoteId: String) = "record/script-edit/$voiceNoteId"

    // 검색 화면
    const val SEARCH = "record/search/"

    // 검색 화면 - 임시?
    fun searchTemp(searchType: SearchType) = "search/${searchType.name}"

}

/**
 * 검색 진입 출처를 정의하는 Enum
 *
 * @author sehoon
 * @since 2026. 5. 10.
 */
enum class SearchType {
    HOME,       // 홈 - 휴지통 빼고 전체 조회
    FOLDER,     // 폴더목록 - 폴더만 조회
    VOICE_NOTE, // 폴더 상세 내 파일 목록 - voiceNote만 조회
    TRASH,      // 휴지통 - 휴지통만 조회
}