package com.umc.product.notice.application.service.query;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NoticeAudienceResolver {

    private final GetChallengerUseCase getChallengerUseCase;
    private final ListChallengerRoleUseCase listChallengerRoleUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public List<Long> resolve(NoticeTargetInfo target) {
        if (target.isStaffNotice()) {
            return resolveStaff(target);
        }
        return resolveChallengers(target);
    }

    private List<Long> resolveChallengers(NoticeTargetInfo target) {
        List<ChallengerBasicInfo> challengers = loadChallengers(target.targetGisuId());
        if (challengers.isEmpty()) {
            return List.of();
        }
        Set<Long> memberIds = challengers.stream().map(ChallengerBasicInfo::memberId).collect(Collectors.toSet());
        boolean hasOrganizationTarget = target.targetSchoolId() != null || target.targetChapterId() != null;
        Map<Long, Long> schoolIdByMemberId = hasOrganizationTarget
            ? getMemberUseCase.findAllSchoolIdsByIds(memberIds) : Map.of();
        // 학교가 없는 회원도 전체 공지에는 포함하고, 존재하지 않는 회원은 기존처럼 제외한다.
        Set<Long> existingMemberIds = hasOrganizationTarget
            ? schoolIdByMemberId.keySet() : getMemberUseCase.findAllNamesByIds(memberIds).keySet();
        Map<Long, ChapterInfo> chapterBySchoolId = loadChaptersForTarget(target, schoolIdByMemberId);

        Set<Long> recipientMemberIds = new LinkedHashSet<>();
        for (ChallengerBasicInfo challenger : challengers) {
            if (!existingMemberIds.contains(challenger.memberId())) {
                continue;
            }
            Long schoolId = schoolIdByMemberId.get(challenger.memberId());
            ChapterInfo chapter = schoolId == null ? null : chapterBySchoolId.get(schoolId);
            Long chapterId = chapter == null ? null : chapter.id();
            if (target.isTarget(challenger.gisuId(), chapterId, schoolId, challenger.part())) {
                recipientMemberIds.add(challenger.memberId());
            }
        }
        return List.copyOf(recipientMemberIds);
    }

    private List<ChallengerBasicInfo> loadChallengers(Long gisuId) {
        if (gisuId != null) {
            return getChallengerUseCase.listBasicByGisuId(gisuId);
        }
        return getChallengerUseCase.getAllLatestGisuPerMemberWithoutChallengerPoints().stream()
            .map(info -> new ChallengerBasicInfo(info.challengerId(), info.memberId(), info.gisuId(),
                info.part(), info.infra(), info.challengerStatus()))
            .toList();
    }

    private Map<Long, ChapterInfo> loadChaptersForTarget(NoticeTargetInfo target, Map<Long, Long> schoolIdByMemberId) {
        if (target.targetChapterId() == null || schoolIdByMemberId.isEmpty()) {
            return Map.of();
        }
        return getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds(Set.of(target.targetGisuId()),
            new HashSet<>(schoolIdByMemberId.values())).getOrDefault(target.targetGisuId(), Map.of());
    }

    private List<Long> resolveStaff(NoticeTargetInfo target) {
        List<ChallengerBasicInfo> challengers = getChallengerUseCase.listBasicByGisuId(target.targetGisuId());
        Map<Long, Long> memberIdByChallengerId = challengers.stream().collect(Collectors.toMap(
            ChallengerBasicInfo::challengerId, ChallengerBasicInfo::memberId));
        if (memberIdByChallengerId.isEmpty()) {
            return List.of();
        }
        return listChallengerRoleUseCase.listByChallengerIdsAndGisuId(memberIdByChallengerId.keySet(), target.targetGisuId())
            .stream()
            .filter(role -> target.isStaffTarget(role.roleType(), role.gisuId(), role.organizationId(),
                role.responsiblePart()))
            .map(role -> memberIdByChallengerId.get(role.challengerId()))
            .distinct()
            .toList();
    }
}
