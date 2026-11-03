package com.umc.product.member.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.member.application.port.in.command.dto.EditBusinessCardCommand;
import com.umc.product.member.domain.MemberProfileLinkType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 명함 수정 요청입니다. 연락처·링크는 명함 수정 화면 조회 응답과 같은 모양입니다.
 */
public record EditBusinessCardRequest(
    @Schema(description = "새 프로필 사진 파일 ID. 생략하면 기존 사진을 유지합니다.", nullable = true)
    String profileImageId,
    @NotNull @Valid ContactRequest contact,
    @Schema(description = "링크 전체. 목록에 없는 타입의 링크는 삭제됩니다. 타입은 중복될 수 없습니다.")
    @NotNull List<@NotNull @Valid LinkRequest> links
) {

    public EditBusinessCardCommand toCommand(Long memberId) {
        return new EditBusinessCardCommand(
            memberId,
            profileImageId,
            new EditBusinessCardCommand.Contact(contact.email(), contact.isPublic()),
            links.stream()
                .map(link -> new EditBusinessCardCommand.Link(link.type(), link.link(), link.isPublic()))
                .toList()
        );
    }

    @AssertTrue(message = "링크 타입은 중복될 수 없습니다.") @Schema(hidden = true)
    public boolean isLinkTypesUnique() {
        if (links == null || links.stream().anyMatch(link -> link == null || link.type() == null)) {
            return true; // @NotNull에서 처리
        }
        return links.stream().map(LinkRequest::type).distinct().count() == links.size();
    }

    public record ContactRequest(
        @Schema(description = "연락처 이메일. 로그인 이메일과 별개입니다. 비우면 삭제합니다.", nullable = true)
        @Email @Size(max = 100) String email,
        @NotNull Boolean isPublic
    ) {
    }

    public record LinkRequest(
        @NotNull MemberProfileLinkType type,
        @Schema(description = "링크. 비우면 삭제합니다.", nullable = true)
        String link,
        @NotNull Boolean isPublic
    ) {
    }
}
