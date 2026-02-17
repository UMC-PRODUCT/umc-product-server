package com.umc.product.member.adapter.in.web.dto.request;


import com.umc.product.member.domain.LinkTypeAndLink;
import java.util.List;

public record EditMemberInfoRequest(
    String profileImageId,
    // TODO: 프로필 내 링크 (깃헙, 링크드인 등) 변경 기능도 추가 예정
    List<LinkTypeAndLink> links
) {
}
