package com.umc.product.maintenance.application.port.out;

import com.umc.product.maintenance.domain.MaintenanceWindow;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoadMaintenanceWindowPort {

    /**
     * 주어진 시각 기준 활성 윈도우 1건.
     */
    Optional<MaintenanceWindow> findActiveAt(Instant now);

    /**
     * 주어진 시각 이후 시작 예정인 가장 가까운 윈도우.
     */
    Optional<MaintenanceWindow> findNextUpcoming(Instant now);

    /**
     * id 로 단건 조회. 없으면 empty.
     */
    Optional<MaintenanceWindow> findById(Long id);

    /**
     * 주어진 시간 범위와 겹치는 (forced_ended_at IS NULL) 윈도우들.
     * 윈도우 생성 시 겹침 검증에 사용한다.
     */
    List<MaintenanceWindow> findOverlapping(Instant startAt, Instant endAt);

    /**
     * 전체 윈도우 (forced_ended_at 무관) — 생성일 내림차순.
     */
    List<MaintenanceWindow> findAllOrderByCreatedAtDesc();
}
