package com.roro.core.navigation

/**
 * 기능 설명: 네비게이션 Route 정리
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
object Routes{
    // 바텀 네비게이션 O
    const val STORAGE = "storage"
    const val RECORDER = "record"

    // 바텀 네비게이션 X
    const val STORAGE_DETAIL = "storage/detail/{folderId}"
    fun storageDetail(folderId: String) = "storage/detail/$folderId"

    const val RECORD_DETAIL = "recoder/detail/{fileId}"
    fun recorderDetail(fileId: String) = "recoder/detail/$fileId"

}