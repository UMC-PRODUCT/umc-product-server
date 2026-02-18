package com.umc.product.challenger.adapter.in.web;

import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 챌린저 ID를 기반으로 회원 및 기수 정보를 포함한 응답 객체를 조립하는 헬퍼 컴포넌트입니다.
 */
@Component
@RequiredArgsConstructor
public class ChallengerResponseAssembler {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;

    public ChallengerInfoResponse fromChallengerId(Long challengerId) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(challengerInfo.memberId());
        GisuInfo gisuInfo = getGisuUseCase.getById(challengerInfo.gisuId());
        ChapterInfo chapterInfo = getChapterUseCase.byGisuAndSchool(challengerInfo.gisuId(), memberInfo.schoolId());

        return ChallengerInfoResponse.from(challengerInfo, memberInfo, gisuInfo, chapterInfo);
    }

    public List<ChallengerInfoResponse> fromMemberId(Long memberId) {
        List<ChallengerInfo> challengerInfos = getChallengerUseCase.getMemberChallengerList(memberId);
        MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(memberId);

        return challengerInfos.stream()
            .map(challengerInfo -> {
                GisuInfo gisuInfo = getGisuUseCase.getById(challengerInfo.gisuId());
                ChapterInfo chapterInfo = getChapterUseCase.byGisuAndSchool(challengerInfo.gisuId(),
                    memberInfo.schoolId());

                return ChallengerInfoResponse.from(challengerInfo, memberInfo, gisuInfo, chapterInfo);
            })
            .toList();
    }
}
