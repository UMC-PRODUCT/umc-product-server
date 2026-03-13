package com.umc.product.schedule.application.port.in.query;

import com.umc.product.schedule.application.port.in.query.dto.PendingAttendancesByScheduleInfo;
import com.umc.product.schedule.application.port.in.query.dto.ScheduleWithStatsInfo;
import java.util.List;

public interface GetScheduleListUseCase {

    /**
     * - 1. 스케줄 목록 조회 - 진행 중인 것 우선 - 이후 종료된 것 최신순 - 2. 특정 스케줄의 출석 통계 조회도 함께 조회 - 3. 중앙 운영진은 기수 전체 일정 조회, 학교 운영진은 본인 생성
     * 일정만 조회
     *
     * @param memberId 현재 로그인한 회원 ID
     */
    List<ScheduleWithStatsInfo> getAll(Long memberId);

    /**
     * 역할 기반 승인 대기 출석 목록 조회 (일정별 그룹핑)
     * <p>
     * - 중앙 운영진 : 본인 참석 일정 (일정 생성 시 본인이 무조건 추가되므로 본인 생성 여부 필터링은 생략) - 교내 회장단 : 교내 챌린저가 파트장으로 있는 스터디 그룹 일정 + 본인이 생성한 일정 -
     * 교내 파트장 : 본인이 파트장으로 있는 스터디 그룹 일정 + 본인이 생성한 일정 - 기타 운영진 : 본인이 생성한 일정
     *
     * @param memberId 현재 로그인한 회원 ID
     * @return 일정별 승인 대기 출석 목록
     */
    List<PendingAttendancesByScheduleInfo> getAllPendingByRole(Long memberId);
}
