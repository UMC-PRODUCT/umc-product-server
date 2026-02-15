package com.umc.product.schedule.application.service;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.schedule.domain.Schedule;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Schedule 도메인 권한 평가에 사용되는 공통 헬퍼 메서드 제공
 */
@Component
@RequiredArgsConstructor
public class SchedulePermissionHelper {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    /**
     * 일정 작성자 본인인지 확인
     */
    public boolean isAuthor(Long memberId, Schedule schedule) {
        Long authorChallengerId = schedule.getAuthorChallengerId();
        Long authorMemberId = getChallengerUseCase.getChallengerPublicInfo(authorChallengerId).memberId();

        return Objects.equals(memberId, authorMemberId);
    }

    /**
     * 해당 기수에서 운영진(ChallengerRoleType이 있는 사람)인지 확인
     */
    public boolean isStaffInGisu(Long memberId, Long gisuId) {
        return !getChallengerRoleUseCase.getRolesByGisu(memberId, gisuId).isEmpty();
    }

    /**
     * 해당 기수의 중앙 총괄단인지 확인
     */
    public boolean isCentralCoreInGisu(Long memberId, Long gisuId) {
        return getChallengerRoleUseCase.isCentralCoreInGisu(memberId, gisuId);
    }

    /**
     * 운영진 또는 총괄 여부 확인 (ChallengerRole 보유 여부)
     */
    public boolean hasAnyRole(SubjectAttributes subjectAttributes) {
        return !subjectAttributes.roleAttributes().isEmpty();
    }

    /**
     * 출석 관리 권한 확인: 해당 기수 중앙 총괄단 OR (일정 작성자 본인 AND 해당 기수 운영진)
     */
    public boolean canManageAttendance(Long memberId, Schedule schedule, Long gisuId) {
        // 해당 기수 중앙 총괄단이면 OK
        if (isCentralCoreInGisu(memberId, gisuId)) {
            return true;
        }

        // 일정 작성자 본인 AND 해당 기수 운영진이면 OK
        return isAuthor(memberId, schedule) && isStaffInGisu(memberId, gisuId);
    }

    /**
     * 일정의 기수 ID 조회 (authorChallengerId 기반)
     */
    public Long getGisuIdFromSchedule(Schedule schedule) {
        Long authorChallengerId = schedule.getAuthorChallengerId();
        return getChallengerUseCase.getChallengerPublicInfo(authorChallengerId).gisuId();
    }
}
