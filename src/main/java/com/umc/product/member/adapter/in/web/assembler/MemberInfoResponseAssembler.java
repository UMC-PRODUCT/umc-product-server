package com.umc.product.member.adapter.in.web.assembler;

import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.adapter.in.web.dto.response.MemberInfoResponse;
import com.umc.product.member.application.port.in.query.GetMemberProfileUseCase;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.MemberProfileInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberInfoResponseAssembler {

    private final GetMemberUseCase getMemberUseCase;
    private final GetMemberProfileUseCase getMemberProfileUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public MemberInfoResponse fromMemberId(Long memberId) {
        MemberInfo memberInfo = getMemberUseCase.getById(memberId);

        // 챌린저 별로 gisu/chapter를 매번 조회하면 N+1이 발생하므로, 두 도메인의 batch 메서드로 한 번에 조회합니다.
        List<ChallengerInfo> challengerInfos = getChallengerUseCase.getAllByMemberId(memberId);

        Set<Long> gisuIds = challengerInfos.stream()
            .map(ChallengerInfo::gisuId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Long, GisuInfo> gisuByGisuId = gisuIds.isEmpty()
            ? Map.of()
            : getGisuUseCase.getByIds(gisuIds).stream()
                .collect(Collectors.toMap(GisuInfo::gisuId, g -> g));

        Map<Long, Map<Long, ChapterInfo>> chapterByGisuAndSchool = (gisuIds.isEmpty() || memberInfo.schoolId() == null)
            ? Map.of()
            : getChapterUseCase.getChapterMapByGisuIdsAndSchoolIds(gisuIds, Set.of(memberInfo.schoolId()));

        List<ChallengerInfoResponse> challengerInfoResponses = challengerInfos.stream()
            .map(info -> ChallengerInfoResponse.from(
                info,
                memberInfo,
                gisuByGisuId.get(info.gisuId()),
                chapterByGisuAndSchool.getOrDefault(info.gisuId(), Map.of()).get(memberInfo.schoolId())
            ))
            .toList();

        MemberProfileInfo memberProfileInfo = getMemberProfileUseCase.getMemberProfileById(memberId);

        return MemberInfoResponse.from(memberInfo, memberProfileInfo, challengerInfoResponses);
    }

    public MemberInfoResponse fromMemberIdToPublic(Long memberId) {
        return fromMemberId(memberId).toPublic();
    }
}
