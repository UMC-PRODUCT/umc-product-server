package com.umc.product.member.adapter.in.web.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.adapter.in.web.dto.response.ChallengerInfoResponse;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.MemberProfileInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 사용자 정보를 응답하는 DTO 입니다.
 * <p>
 * 현재는 내 프로필 조회와 남의 프로필 조회를 동일한 Response를 사용하고 있는데,
 * <p>
 * 추후 Public/Private 정보를 구분해서 사용해야 할 경우 분리가 필요합니다.
 */
@Builder
public record MemberInfoResponse(
    Long id,
    String name,
    String nickname,
    String email,
    Long schoolId,
    String schoolName,
    String profileImageLink,
    MemberStatus status,
    List<ChallengerRoleInfo> roles, // TODO: 이거 그대로 써도 괜찮은거 맞나?
    List<ChallengerInfoResponse> challengerRecords,
    MemberProfileInfo profile,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "본인 응답에만 포함하는 프로덕트 문의 관리 가능 여부. UMC_PRODUCT 또는 SUPER_ADMIN이면 true")
    Boolean isUmcProduct
    // TODO: 활동 이력이나 각종 링크들 추가해야 함
) {
    public static MemberInfoResponse from(MemberInfo info, List<ChallengerInfoResponse> challengerRecords) {
        return MemberInfoResponse.builder()
            .id(info.id())
            .name(info.name())
            .nickname(info.nickname())
            .email(info.email())
            .schoolId(info.schoolId())
            .schoolName(info.schoolName())
            .profileImageLink(info.profileImageLink())
            .status(info.status())
            .roles(info.roles())
            .challengerRecords(challengerRecords)
            .build();
    }

    public static MemberInfoResponse from(
        MemberInfo info, MemberProfileInfo profileInfo,
        List<ChallengerInfoResponse> challengerRecords, Boolean isUmcProduct
    ) {
        return MemberInfoResponse.builder()
            .id(info.id())
            .name(info.name())
            .nickname(info.nickname())
            .email(info.email())
            .schoolId(info.schoolId())
            .schoolName(info.schoolName())
            .profileImageLink(info.profileImageLink())
            .status(info.status())
            .roles(info.roles())
            .challengerRecords(challengerRecords)
            .profile(profileInfo)
            .isUmcProduct(isUmcProduct)
            .build();
    }

    public MemberInfoResponse toPublic() {
        return MemberInfoResponse.builder()
            .id(id)
            .name(name)
            .nickname(nickname)
            .email(null)
            .schoolId(schoolId)
            .schoolName(schoolName)
            .profileImageLink(profileImageLink)
            .status(null)
            .roles(roles)
            .challengerRecords(challengerRecords.stream().map(ChallengerInfoResponse::toPublic).toList())
            .profile(profile)
            .build();
    }
}
