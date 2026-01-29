package com.umc.product.member.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30) // 한글 10자까지 고려
    private String name;

    @Column(nullable = false, length = 20) // 한글 1~5자
    private String nickname;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "school_id")
    private Long schoolId;  // ID 참조만 (organization 도메인 의존 방지)

    @Column(name = "profile_image_id")
    private String profileImageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Builder
    private Member(String name, String nickname, String email, Long schoolId, String profileImageId) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.schoolId = schoolId;
        this.profileImageId = profileImageId;
        this.status = MemberStatus.ACTIVE;
    }

    // Domain Logic: 프로필 업데이트
    public void updateProfile(String nickname, Long schoolId, String profileImageId) {
        validateActive();
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (schoolId != null) {
            this.schoolId = schoolId;
        }
        if (profileImageId != null) {
            this.profileImageId = profileImageId;
        }
    }

    /**
     * 유효한 사용자인지 검증 (휴면 회원이 아닌지)
     */
    private void validateActive() {
        if (this.status != MemberStatus.ACTIVE) {
            throw new MemberDomainException(MemberErrorCode.MEMBER_NOT_ACTIVE);
        }
    }

    // TODO: 탈퇴 및 휴면 처리에 대한 도메인 로직은 추후 추가
}
