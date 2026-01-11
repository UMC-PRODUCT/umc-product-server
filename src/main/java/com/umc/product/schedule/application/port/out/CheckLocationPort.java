package com.umc.product.schedule.application.port.out;

import com.umc.product.schedule.domain.vo.Location;
import com.umc.product.schedule.domain.vo.LocationRange;

/**
 * 위치 계산 Port 두 지점 간의 거리 계산 및 범위 확인을 담당
 */
public interface CheckLocationPort {

    /**
     * 두 위치 간의 거리 계산 (미터 단위)
     *
     * @param from 시작 위치
     * @param to   도착 위치
     * @return 거리 (미터)
     */
    double calculateDistance(Location from, Location to);

    /**
     * 사용자 위치가 목표 위치의 범위 내에 있는지 확인
     *
     * @param target 목표 위치 (출석부 위치)
     * @param user   사용자 위치 (실제 체크한 위치)
     * @param range  허용 범위
     * @return 범위 내 여부
     */
    boolean isWithinRange(Location target, Location user, LocationRange range);
}
