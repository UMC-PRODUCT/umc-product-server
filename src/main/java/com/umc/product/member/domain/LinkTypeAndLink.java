package com.umc.product.member.domain;

public record LinkTypeAndLink(
    MemberProfileLinkType type,
    String link
) {
}
