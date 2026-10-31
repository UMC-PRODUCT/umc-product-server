package com.umc.product.member.application.port.in.query.dto;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.domain.MemberProfileLinkType;

/**
 * 명함에 노출되는 정보입니다.
 * <p>
 * 다른 회원에게 보이는 그대로를 담습니다. 비공개로 설정했거나 값이 없는 연락처·링크는 포함하지 않습니다.
 *
 * @param activity 활동 이력 중 가장 최신 기수의 기수·파트. 활동 이력이 없으면 null
 * @param contact  공개로 설정한 연락처. 비공개이거나 입력하지 않았으면 null
 * @param links    공개로 설정했고 열 수 있는 형식의 링크
 */
public record BusinessCardInfo(
    Long memberId,
    String name,
    String nickname,
    String schoolName,
    String profileImageLink,
    Activity activity,
    Contact contact,
    List<Link> links
) {

    /**
     * @param part 파트. 파트가 없는 챌린저는 null
     */
    public record Activity(Long gisuId, Long generation, ChallengerPart part) {
    }

    /**
     * @param email 연락처 이메일. 로그인 이메일과 별개인 선택 입력값이며, 입력하지 않았으면 null
     */
    public record Contact(String email, boolean isPublic) {
    }

    /**
     * @param link 링크. 입력하지 않았으면 null
     */
    public record Link(MemberProfileLinkType type, String link, boolean isPublic) {
    }
}
