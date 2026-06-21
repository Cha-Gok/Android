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
    const val SEARCH = "search/{searchType}?folderId={folderId}" // 검색
    const val TRASH_VOICE_NOTE = "trash_voiceNote/{voiceNoteId}" // 휴지통 -> 상세정보

    const val SETTINGS = "settings" // 설정


    // 스토리지 화면 구성
    // 폴더 목록
    const val STORAGE_FOLDER = "storage/folder"

    // 폴더 목록 > 파일 목록
    const val STORAGE_FILE = "storage/folder/file/{folderId}/{folderName}/{isTrash}"
    fun storageFile(
        folderId: String, folderName: String, isTrash: Boolean
    ) = "storage/folder/file/${folderId}/${folderName}/${isTrash}"


    // 녹음 결과 화면 추가
    const val RECORD_RESULT = "record/result/{voiceNoteId}"
    fun recordResult(voiceNoteId: String) = "record/result/$voiceNoteId"

    // ✅ 추가 - voiceNoteId 없이 스켈레톤만 보여주는 화면
    const val RECORD_RESULT_WAITING = "record/waiting"

    // 스크립트 편집 화면
    const val SCRIPT_EDIT = "record/script-edit/{voiceNoteId}"
    fun scriptEdit(voiceNoteId: String) = "record/script-edit/$voiceNoteId"

    // 검색 결과 검색 화면
    const val SEARCH_VOICENOTE = "record/search/"

    // 검색 화면
    fun search(searchType: SearchType, folderId: String? = null): String {
        return if (folderId != null) {
            "search/${searchType.name}?folderId=$folderId"
        } else {
            "search/${searchType.name}"
        }
    }

    fun trashVoiceNote(voiceNoteId: String): String {
        return "trash_voiceNote/${voiceNoteId}"
    }
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
    TRASH;     // 휴지통 - 휴지통만 조회

    companion object {
        fun fromString(value: String?): SearchType {
            return entries.find { it.name == value } ?: HOME
        }
    }
}