package com.umc.product.global.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * LocalDateTime을 KST(Asia/Seoul) 타임존으로 해석하여 DB에 저장/조회하는 컨버터
 *
 * <p>사용 목적:
 * <ul>
 *   <li>출석 도메인의 일정 시간은 KST 기준으로 관리됨</li>
 *   <li>DB는 UTC로 저장하되, 애플리케이션 레벨에서는 KST LocalDateTime으로 처리</li>
 *   <li>다른 도메인(Instant 사용)에는 영향 없음 (autoApply = false)</li>
 * </ul>
 *
 * <p>변환 로직:
 * <ul>
 *   <li>저장: LocalDateTime(KST) → Timestamp(UTC)</li>
 *   <li>조회: Timestamp(UTC) → LocalDateTime(KST)</li>
 * </ul>
 *
 * <p>적용 대상:
 * <ul>
 *   <li>Schedule.startsAt, endsAt</li>
 *   <li>AttendanceWindow.startTime, endTime</li>
 *   <li>AttendanceRecord.checkedAt, confirmedAt</li>
 * </ul>
 */
@Converter  // autoApply = false (명시적으로 @Convert 어노테이션 필요)
public class KstLocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * Entity의 LocalDateTime을 DB의 Timestamp(UTC)로 변환
     *
     * @param localDateTime KST 기준 LocalDateTime
     * @return UTC 기준 Timestamp
     */
    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        // LocalDateTime을 KST로 해석하여 UTC Timestamp로 변환
        // 예: 2024-02-18 16:00 (KST) → 2024-02-18 07:00 (UTC)
        return Timestamp.from(localDateTime.atZone(KST).toInstant());
    }

    /**
     * DB의 Timestamp(UTC)를 Entity의 LocalDateTime(KST)으로 변환
     *
     * @param timestamp UTC 기준 Timestamp
     * @return KST 기준 LocalDateTime
     */
    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        // UTC Timestamp를 KST LocalDateTime으로 변환
        // 예: 2024-02-18 07:00 (UTC) → 2024-02-18 16:00 (KST)
        return timestamp.toInstant().atZone(KST).toLocalDateTime();
    }
}
