package com.umc.product.member.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.dto.BusinessCardInfo;
import com.umc.product.member.domain.MemberProfileLinkType;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 명함 응답입니다. 연락처·링크는 명함 수정 요청과 같은 모양이며, 공개로 설정한 항목만 포함합니다.
 */
public record BusinessCardResponse(
    Long memberId,
    String name,
    String nickname,
    String schoolName,
    String profileImageLink,
    @Schema(description = "활동 이력 중 가장 최신 기수의 기수·파트. 활동 이력이 없으면 null", nullable = true)
    ActivityResponse activity,
    @Schema(description = "공개로 설정한 연락처. 비공개이거나 입력하지 않았으면 null", nullable = true)
    ContactResponse contact,
    @Schema(description = "공개로 설정했고 열 수 있는(http/https) 링크")
    List<LinkResponse> links
) {

    public static BusinessCardResponse from(BusinessCardInfo info) {
        return new BusinessCardResponse(
            info.memberId(),
            info.name(),
            info.nickname(),
            info.schoolName(),
            info.profileImageLink(),
            ActivityResponse.from(info.activity()),
            info.contact() == null ? null : ContactResponse.from(info.contact()),
            info.links().stream().map(LinkResponse::from).toList()
        );
    }

    public record ActivityResponse(
        Long gisuId,
        Long generation,
        @Schema(description = "파트. 파트가 없는 챌린저는 null", nullable = true)
        ChallengerPart part
    ) {
        static ActivityResponse from(BusinessCardInfo.Activity activity) {
            if (activity == null) {
                return null;
            }
            return new ActivityResponse(activity.gisuId(), activity.generation(), activity.part());
        }
    }

    public record ContactResponse(
        @Schema(description = "연락처 이메일. 로그인 이메일과 별개입니다.", nullable = true)
        String email,
        boolean isPublic
    ) {
        static ContactResponse from(BusinessCardInfo.Contact contact) {
            return new ContactResponse(contact.email(), contact.isPublic());
        }
    }

    public record LinkResponse(
        MemberProfileLinkType type,
        @Schema(nullable = true)
        String link,
        boolean isPublic
    ) {
        static LinkResponse from(BusinessCardInfo.Link link) {
            return new LinkResponse(link.type(), link.link(), link.isPublic());
        }
    }
}
