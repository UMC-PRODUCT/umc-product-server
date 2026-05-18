package com.umc.product.maintenance.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.maintenance.exception.MaintenanceDomainException;
import com.umc.product.maintenance.exception.MaintenanceErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "maintenance_window")
public class MaintenanceWindow extends BaseEntity {

    private static final long START_AT_GRACE_SECONDS = 60L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, name = "scope")
    private MaintenanceScope scope;

    @Convert(converter = MaintenanceDomainSetConverter.class)
    @Column(length = 512, name = "target_domains")
    private Set<MaintenanceDomain> targetDomains;

    @Column(nullable = false, name = "start_at")
    private Instant startAt;

    @Column(nullable = false, name = "end_at")
    private Instant endAt;

    @Column(nullable = false, length = 255, name = "title")
    private String title;

    @Column(nullable = false, length = 1000, name = "message")
    private String message;

    @Column(name = "forced_ended_at")
    private Instant forcedEndedAt;

    @Column(name = "forced_ended_by")
    private Long forcedEndedBy;

    @Column(nullable = false, name = "created_by")
    private Long createdBy;

    @Builder(access = AccessLevel.PRIVATE)
    private MaintenanceWindow(
        MaintenanceScope scope,
        Set<MaintenanceDomain> targetDomains,
        Instant startAt,
        Instant endAt,
        String title,
        String message,
        Long createdBy
    ) {
        this.scope = scope;
        this.targetDomains = targetDomains;
        this.startAt = startAt;
        this.endAt = endAt;
        this.title = title;
        this.message = message;
        this.createdBy = createdBy;
    }

    /**
     * 새 점검 윈도우 생성. 시간/스코프 무결성 검증 포함.
     */
    public static MaintenanceWindow of(
        MaintenanceScope scope,
        Set<MaintenanceDomain> targetDomains,
        Instant startAt,
        Instant endAt,
        String title,
        String message,
        Long createdBy,
        Instant now
    ) {
        validateTimeRange(startAt, endAt, now);
        Set<MaintenanceDomain> normalizedDomains = normalizeDomains(scope, targetDomains);
        return MaintenanceWindow.builder()
            .scope(scope)
            .targetDomains(normalizedDomains)
            .startAt(startAt)
            .endAt(endAt)
            .title(title)
            .message(message)
            .createdBy(createdBy)
            .build();
    }

    /**
     * 현재 시각 기준 활성(점검 중) 여부.
     */
    public boolean isActiveAt(Instant now) {
        return forcedEndedAt == null
            && !startAt.isAfter(now)
            && endAt.isAfter(now);
    }

    /**
     * 미래 예약 상태 여부 (아직 시작 전).
     */
    public boolean isUpcomingAt(Instant now) {
        return forcedEndedAt == null && startAt.isAfter(now);
    }

    /**
     * 강제 종료. 이미 종료된 윈도우라면 예외.
     *
     * @param now           현재 시각
     * @param requestedBy   종료를 요청한 운영자 memberId (감사 목적). null 허용
     */
    public void forceEnd(Instant now, Long requestedBy) {
        if (forcedEndedAt != null) {
            throw new MaintenanceDomainException(MaintenanceErrorCode.ALREADY_ENDED);
        }
        if (!endAt.isAfter(now)) {
            throw new MaintenanceDomainException(MaintenanceErrorCode.ALREADY_ENDED);
        }
        this.forcedEndedAt = now;
        this.forcedEndedBy = requestedBy;
    }

    /**
     * 주어진 URI 가 본 윈도우의 차단 대상인지 판정. 활성 상태가 아니어도 호출 가능
     * (필터에서 활성 판정 이후 호출되는 것을 가정).
     */
    public boolean blocks(String requestUri) {
        if (scope == MaintenanceScope.FULL) {
            return true;
        }
        return MaintenanceDomain.fromUri(requestUri)
            .map(targetDomains::contains)
            .orElse(false);
    }

    private static void validateTimeRange(Instant startAt, Instant endAt, Instant now) {
        if (!endAt.isAfter(startAt)) {
            throw new MaintenanceDomainException(MaintenanceErrorCode.INVALID_TIME_RANGE);
        }
        Instant graceFloor = now.minusSeconds(START_AT_GRACE_SECONDS);
        if (startAt.isBefore(graceFloor)) {
            throw new MaintenanceDomainException(MaintenanceErrorCode.START_AT_IN_PAST);
        }
    }

    private static Set<MaintenanceDomain> normalizeDomains(
        MaintenanceScope scope,
        Set<MaintenanceDomain> domains
    ) {
        if (scope == MaintenanceScope.FULL) {
            return EnumSet.noneOf(MaintenanceDomain.class);
        }
        if (domains == null || domains.isEmpty()) {
            throw new MaintenanceDomainException(MaintenanceErrorCode.TARGET_DOMAINS_REQUIRED);
        }
        return EnumSet.copyOf(domains);
    }
}
