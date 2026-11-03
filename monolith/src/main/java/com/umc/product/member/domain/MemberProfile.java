package com.umc.product.member.domain;

import java.util.List;
import java.util.Map;

import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * MemberProfile은 Member를 통해서만 조회되도록 구성됩니다.
 * <p>
 * 외부 링크·연락처 이메일과 함께 각 항목의 명함 공개 여부를 가집니다. 공개 여부의 초기값은 모두 공개입니다.
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

    /**
     * 명함에 노출하는 연락처 이메일입니다. 로그인 이메일({@link Member#getEmail()})과 별개인 선택 입력값입니다.
     */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_email_public", nullable = false)
    private boolean contactEmailPublic = true;

    @Column(name = "linked_in_public", nullable = false)
    private boolean linkedInPublic = true;

    @Column(name = "instagram_public", nullable = false)
    private boolean instagramPublic = true;

    @Column(name = "github_public", nullable = false)
    private boolean githubPublic = true;

    @Column(name = "blog_public", nullable = false)
    private boolean blogPublic = true;

    @Column(name = "personal_public", nullable = false)
    private boolean personalPublic = true;

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

    /**
     * 링크 값을 전체 교체합니다. 공개 여부는 링크 값과 별개로 유지됩니다.
     */
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

    /**
     * 연락처 이메일을 교체합니다. 비어 있으면 연락처를 삭제합니다.
     */
    public void updateContactEmail(String contactEmail) {
        this.contactEmail = (contactEmail == null || contactEmail.isBlank()) ? null : contactEmail.trim();
    }

    /**
     * 공개 여부를 변경합니다.
     * <p>
     * 공개 설정을 모르는 클라이언트도 링크를 전체 교체하므로, 값이 주어지지 않은 항목(null 또는 Map에 없는 타입)은 기존 설정을 유지합니다.
     *
     * @param contactEmailPublic 연락처 이메일 공개 여부. null이면 유지
     * @param linkVisibilities   링크 타입별 공개 여부. 포함되지 않은 타입은 유지
     */
    public void changeVisibility(Boolean contactEmailPublic, Map<MemberProfileLinkType, Boolean> linkVisibilities) {
        if (contactEmailPublic != null) {
            this.contactEmailPublic = contactEmailPublic;
        }

        linkVisibilities.forEach((type, isPublic) -> {
            if (isPublic == null) {
                return;
            }
            switch (type) {
                case LINKEDIN -> this.linkedInPublic = isPublic;
                case INSTAGRAM -> this.instagramPublic = isPublic;
                case GITHUB -> this.githubPublic = isPublic;
                case BLOG -> this.blogPublic = isPublic;
                case PERSONAL -> this.personalPublic = isPublic;
            }
        });
    }

    public String getLink(MemberProfileLinkType type) {
        return switch (type) {
            case LINKEDIN -> linkedIn;
            case INSTAGRAM -> instagram;
            case GITHUB -> github;
            case BLOG -> blog;
            case PERSONAL -> personal;
        };
    }

    public boolean isLinkPublic(MemberProfileLinkType type) {
        return switch (type) {
            case LINKEDIN -> linkedInPublic;
            case INSTAGRAM -> instagramPublic;
            case GITHUB -> githubPublic;
            case BLOG -> blogPublic;
            case PERSONAL -> personalPublic;
        };
    }
}
