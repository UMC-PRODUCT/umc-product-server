package com.umc.product.notification.application.port.out;

import java.util.List;

public interface LoadFcmTopicPort {

    List<String> findTopicNamesByFcmTokenId(Long fcmTokenId);
}
