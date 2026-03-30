package com.roro.core.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 기능 설명:
 * - 사용자가 생성한 폴더 정보를 저장하는 테이블
 * - 녹음 파일 및 메모를 그룹화하기 위한 기준 데이터
 *
 * 컬럼 설명:
 * @property id         : 폴더의 고유 ID (Primary Key)
 * @property name       : 폴더 이름
 * @property deletedAt  : 삭제 시간
 * @property createdAt  : 생성 시간
 * @property updatedAt  : 수정 시간
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Entity(
    tableName = "folder",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["deletedAt", "updatedAt"])
    ]
)
data class FolderEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val deletedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
