package com.umc.product.notification.domain;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fcm_token_topic")
public class FcmTokenTopic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fcm_token_id", nullable = false)
    private Long fcmTokenId;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Builder(access = AccessLevel.PRIVATE)
    private FcmTokenTopic(Long fcmTokenId, String topicName) {
        this.fcmTokenId = fcmTokenId;
        this.topicName = topicName;
    }

    public static FcmTokenTopic of(Long fcmTokenId, String topicName) {
        return FcmTokenTopic.builder()
            .fcmTokenId(fcmTokenId)
            .topicName(topicName)
            .build();
    }
}
