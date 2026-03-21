package com.umc.product.notification.adapter.out.persistentce;

import com.umc.product.notification.application.port.out.LoadFcmTopicPort;
import com.umc.product.notification.application.port.out.SaveFcmTopicPort;
import com.umc.product.notification.domain.FcmTokenTopic;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FcmTokenTopicPersistenceAdapter implements LoadFcmTopicPort, SaveFcmTopicPort {

    private final FcmTokenTopicJpaRepository repository;

    @Override
    public List<String> findTopicNamesByFcmTokenId(Long fcmTokenId) {
        return repository.findByFcmTokenId(fcmTokenId).stream()
            .map(FcmTokenTopic::getTopicName)
            .toList();
    }

    @Override
    public boolean existsByFcmTokenIdAndTopicName(Long fcmTokenId, String topicName) {
        return repository.existsByFcmTokenIdAndTopicName(fcmTokenId, topicName);
    }

    @Override
    public void saveTopicSubscription(Long fcmTokenId, String topicName) {
        if (!repository.existsByFcmTokenIdAndTopicName(fcmTokenId, topicName)) {
            repository.save(FcmTokenTopic.of(fcmTokenId, topicName));
        }
    }

    @Override
    public void deleteTopicSubscription(Long fcmTokenId, String topicName) {
        repository.deleteByFcmTokenIdAndTopicName(fcmTokenId, topicName);
    }

    @Override
    public void deleteAllTopicSubscriptions(Long fcmTokenId) {
        repository.deleteByFcmTokenId(fcmTokenId);
    }
}
