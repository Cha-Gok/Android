package com.roro.core.database

import androidx.room.TypeConverter
import com.roro.core.domain.model.SummaryStatus
import java.util.UUID

/**
 * 기능 설명:
 * - Room 데이터베이스에서 직접 저장할 수 없는 타입을
 * DB에 젖아 가능한 타입으로 변환한다.
 *
 * 변환 대상:
 * 1. UUID <-> String
 *  - DB에는 String 저장
 *  - 앱에서는 UUID 객체로 사용
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
class Converters{
    @TypeConverter
    fun fromUuid(uuid: UUID?):String? = uuid?.toString()

    @TypeConverter
    fun toUuid(value: String?): UUID? = value?.let(UUID::fromString)

    @TypeConverter
    fun fromString(value: String?): SummaryStatus {
        return try {
            // DB의 String을 Enum으로 변환
            SummaryStatus.valueOf(value ?: SummaryStatus.NONE.name)
        } catch (e: Exception) {
            SummaryStatus.NONE
        }
    }

    @TypeConverter
    fun statusToString(status: SummaryStatus?): String {
        // Enum을 DB에 저장할 String으로 변환
        return status?.name ?: SummaryStatus.NONE.name
    }
}