package com.umc.product.notice.application.service.query;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
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
        List<ChallengerInfo> challengers = target.targetGisuId() == null
            ? getChallengerUseCase.getAllLatestGisuPerMemberWithoutChallengerPoints()
            : getChallengerUseCase.getAllByGisuId(target.targetGisuId());
        if (challengers.isEmpty()) {
            return List.of();
        }
        Set<Long> memberIds = challengers.stream().map(ChallengerInfo::memberId).collect(Collectors.toSet());
        Map<Long, MemberInfo> memberById = getMemberUseCase.findAllByIds(memberIds);
        Map<Long, Map<Long, ChapterInfo>> chapterByGisuAndSchool =
            loadChaptersForTarget(target, challengers, memberById.values());

        Set<Long> recipientMemberIds = new LinkedHashSet<>();
        for (ChallengerInfo challenger : challengers) {
            MemberInfo member = memberById.get(challenger.memberId());
            if (member == null) {
                continue;
            }
            Long chapterId = findChapterId(chapterByGisuAndSchool, challenger.gisuId(), member.schoolId());
            if (target.isTarget(challenger.gisuId(), chapterId, member.schoolId(), challenger.part())) {
                recipientMemberIds.add(challenger.memberId());
            }
        }
        return List.copyOf(recipientMemberIds);
    }

    private Map<Long, Map<Long, ChapterInfo>> loadChaptersForTarget(NoticeTargetInfo target,
                                                                  List<ChallengerInfo> challengers,
                                                                  Collection<MemberInfo> members) {
        if (target.targetChapterId() == null) {
            return Map.of();
        }
        Set<Long> schoolIds = members.stream().map(MemberInfo::schoolId)
            .filter(Objects::nonNull).collect(Collectors.toSet());
        if (schoolIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> gisuIds = challengers.stream().map(ChallengerInfo::gisuId).collect(Collectors.toSet());
        return getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds(gisuIds, schoolIds);
    }

    private Long findChapterId(Map<Long, Map<Long, ChapterInfo>> chapterByGisuAndSchool, Long gisuId, Long schoolId) {
        if (schoolId == null) {
            return null;
        }
        ChapterInfo chapter = chapterByGisuAndSchool.getOrDefault(gisuId, Map.of()).get(schoolId);
        return chapter == null ? null : chapter.id();
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
