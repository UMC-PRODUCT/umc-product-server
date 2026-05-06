package com.umc.product.project.adapter.in.web.dto.common;

import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import lombok.Builder;

/**
 * 멤버 간략 정보. 여러 Response에 embedded되는 공용 값 객체.
 */
@Builder
public record MemberBrief(
    Long memberId,
    String nickname,
    String name,
    String schoolName
) {
    public static MemberBrief from(MemberInfo info) {
        return MemberBrief.builder()
            .memberId(info.id())
            .nickname(info.nickname())
            .name(info.name())
            .schoolName(info.schoolName())
            .build();
    }
}
