package com.umc.product.community.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 작성자 정보 조회를 담당하는 컴포넌트 챌린저 ID로부터 작성자 이름을 조회하는 공통 로직을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class AuthorInfoProvider {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    /**
     * 챌린저 ID로 작성자 이름 조회
     *
     * @param challengerId 챌린저 ID
     * @return 작성자 이름
     */
    public String getAuthorName(Long challengerId) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(challengerId);
        MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(challengerInfo.memberId());
        return memberInfo.name();
    }
}
