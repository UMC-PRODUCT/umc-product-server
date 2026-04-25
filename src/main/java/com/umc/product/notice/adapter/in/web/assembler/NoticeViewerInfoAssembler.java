package com.umc.product.notice.adapter.in.web.assembler;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeViewerInfo;
import com.umc.product.notice.domain.enums.NoticeTargetRole;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 공지 조회자의 소속 정보를 여러 UseCase를 통해 조립하는 헬퍼 컴포넌트입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeViewerInfoAssembler {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public NoticeViewerInfo toMemberIdAndGisuId(Long memberId, Long gisuId) {
        Set<ChallengerPart> memberParts = resolveParts(memberId, gisuId);
        Set<NoticeTargetRole> staffRoles = resolveStaffRoles(memberId, gisuId);

        MemberInfo memberInfo = getMemberUseCase.findAllByIds(Set.of(memberId)).get(memberId);
        Long schoolId = memberInfo != null ? memberInfo.schoolId() : null;

        Long chapterId = null;
        if (schoolId != null) {
            try {
                chapterId = getChapterUseCase.byGisuAndSchool(gisuId, schoolId).id();
            } catch (Exception e) {
                log.debug("지부 정보 조회 실패 - gisuId={}, schoolId={}: {}", gisuId, schoolId, e.getMessage());
            }
        }

        return new NoticeViewerInfo(memberParts, schoolId, chapterId, staffRoles);
    }

    /**
     * 해당 기수에서 멤버가 볼 수 있는 파트 목록을 조회합니다. - Challenger.part: 챌린저 본인의 소속 파트 - ChallengerRole.responsiblePart: 파트장 역할에서 담당하는
     * 파트 챌린저 정보가 없으면 빈 Set을 반환하여 파트 조건 없는 공지만 노출합니다.
     */
    private Set<ChallengerPart> resolveParts(Long memberId, Long gisuId) {
        if (memberId == null || gisuId == null) {
            return Set.of();
        }

        return getChallengerUseCase.findByMemberIdAndGisuId(memberId, gisuId)
            .map(challenger -> {
                Set<ChallengerPart> parts = new HashSet<>();
                parts.add(challenger.part());
                parts.addAll(getChallengerRoleUseCase.getAllResponsiblePartByMemberIdAndGisuId(memberId, gisuId));
                return parts;
            })
            .orElse(Set.of());
    }

    /**
     * 해당 기수에서 멤버가 조회할 수 있는 운영진 공지 대상 역할 목록을 반환
     */
    private Set<NoticeTargetRole> resolveStaffRoles(Long memberId, Long gisuId) {
        if (memberId == null || gisuId == null) {
            return Set.of();
        }

        if (getChallengerRoleUseCase.isCentralCoreInGisu(memberId, gisuId)) {
            return EnumSet.of(
                NoticeTargetRole.CENTRAL_EDUCATION_TEAM,
                NoticeTargetRole.CENTRAL_OPERATING_TEAM,
                NoticeTargetRole.SCHOOL_PART_LEADER,
                NoticeTargetRole.SCHOOL_PRESIDENT_TEAM
            );
        }

        Set<NoticeTargetRole> roles = EnumSet.noneOf(NoticeTargetRole.class);
        getChallengerRoleUseCase.findAllByMemberId(memberId).stream()
            .filter(role -> gisuId.equals(role.gisuId()))
            .forEach(role -> {
                NoticeTargetRole mapped = toNoticeTargetRole(role.roleType());
                if (mapped != null) {
                    roles.addAll(mapped.readableRoles());
                }
            });
        return roles;
    }

    // ChallengerRoleType → NoticeTargetRole 매핑. 총괄단은 최상단에서 처리되므로 null 반환
    private NoticeTargetRole toNoticeTargetRole(ChallengerRoleType roleType) {
        return switch (roleType) {
            case SCHOOL_PART_LEADER -> NoticeTargetRole.SCHOOL_PART_LEADER;
            case SCHOOL_PRESIDENT, SCHOOL_VICE_PRESIDENT -> NoticeTargetRole.SCHOOL_PRESIDENT_TEAM;
            case CENTRAL_EDUCATION_TEAM_MEMBER -> NoticeTargetRole.CENTRAL_EDUCATION_TEAM;
            case CENTRAL_OPERATING_TEAM_MEMBER -> NoticeTargetRole.CENTRAL_OPERATING_TEAM;
            default -> null;
        };
    }
}
