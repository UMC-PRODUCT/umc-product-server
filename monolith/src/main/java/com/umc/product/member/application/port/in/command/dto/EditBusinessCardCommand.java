package com.umc.product.member.application.port.in.command.dto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.umc.product.member.domain.LinkTypeAndLink;
import com.umc.product.member.domain.MemberProfileLinkType;

/**
 * @param profileImageId 새 프로필 사진 파일 ID. null이면 기존 사진을 유지
 * @param contact        연락처. 이메일을 비우면 삭제
 * @param links          링크 전체. 목록에 없는 타입의 링크는 삭제하고 공개 여부는 유지
 */
public record EditBusinessCardCommand(
    Long memberId,
    String profileImageId,
    Contact contact,
    List<Link> links
) {
    public EditBusinessCardCommand {
        Objects.requireNonNull(contact, "contact must not be null");
        links = List.copyOf(links);
    }

    public List<LinkTypeAndLink> linkTypeAndLinks() {
        return links.stream()
            .map(link -> new LinkTypeAndLink(link.type(), link.link()))
            .toList();
    }

    public Map<MemberProfileLinkType, Boolean> linkVisibilities() {
        return links.stream()
            .collect(Collectors.toMap(Link::type, Link::isPublic));
    }

    public record Contact(String email, boolean isPublic) {
    }

    public record Link(MemberProfileLinkType type, String link, boolean isPublic) {
    }
}
