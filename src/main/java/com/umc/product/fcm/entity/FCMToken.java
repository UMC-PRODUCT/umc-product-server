package com.umc.product.fcm.entity;

import com.umc.product.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FCMToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "fcm_token", columnDefinition = "TEXT")
    private String fcmToken;

    @Builder(access = AccessLevel.PRIVATE)
    private FCMToken(Member member, String fcmToken) {
        this.member = member;
        this.fcmToken = fcmToken;
    }

    public static FCMToken createFCMToken(Member member, String fcmToken) {
        return FCMToken.builder().member(member).fcmToken(fcmToken).build();
    }

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

}
