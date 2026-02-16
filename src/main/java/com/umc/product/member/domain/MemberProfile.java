package com.umc.product.member.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.member.application.port.in.command.dto.UpsertMemberProfileCommand.LinkTypeAndLink;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * MemberProfile은 Member를 통해서만 조회되도록 구성됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_profile")
public class MemberProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "linked_in")
    private String linkedIn;

    private String instagram;

    private String github;

    private String blog;

    private String personal;

    @Builder
    private MemberProfile(String linkedIn, String instagram, String github, String blog, String personal) {
        this.linkedIn = linkedIn;
        this.instagram = instagram;
        this.github = github;
        this.blog = blog;
        this.personal = personal;
    }

    public static MemberProfile fromLinks(List<LinkTypeAndLink> links) {
        MemberProfileBuilder builder = MemberProfile.builder();
        for (LinkTypeAndLink link : links) {
            switch (link.type()) {
                case LINKEDIN -> builder.linkedIn(link.link());
                case INSTAGRAM -> builder.instagram(link.link());
                case GITHUB -> builder.github(link.link());
                case BLOG -> builder.blog(link.link());
                case PERSONAL -> builder.personal(link.link());
            }
        }
        return builder.build();
    }

    public void updateLinks(List<LinkTypeAndLink> links) {
        this.linkedIn = null;
        this.instagram = null;
        this.github = null;
        this.blog = null;
        this.personal = null;

        for (LinkTypeAndLink link : links) {
            switch (link.type()) {
                case LINKEDIN -> this.linkedIn = link.link();
                case INSTAGRAM -> this.instagram = link.link();
                case GITHUB -> this.github = link.link();
                case BLOG -> this.blog = link.link();
                case PERSONAL -> this.personal = link.link();
            }
        }
    }
}
