package com.umc.product.notification.adapter.out.persistentce;

import com.umc.product.notification.domain.FcmTokenTopic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenTopicJpaRepository extends JpaRepository<FcmTokenTopic, Long> {

    List<FcmTokenTopic> findAllByFcmTokenId(Long fcmTokenId);

    boolean existsByFcmTokenIdAndTopicName(Long fcmTokenId, String topicName);

    void deleteByFcmTokenIdAndTopicName(Long fcmTokenId, String topicName);

    void deleteByFcmTokenId(Long fcmTokenId);
}
