package com.umc.product.notification.application.service;

import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import com.umc.product.notification.application.port.out.SaveFcmPort;
import com.umc.product.notification.domain.FcmToken;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmTokenDeactivator {

    private final SaveFcmPort saveFcmPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deactivateInvalidTokens(List<FcmToken> tokens, List<SendResponse> responses) {
        for (int i = 0; i < responses.size(); i++) {
            SendResponse response = responses.get(i);
            if (!response.isSuccessful()
                && response.getException() != null
                && MessagingErrorCode.UNREGISTERED.equals(response.getException().getMessagingErrorCode())) {

                FcmToken token = tokens.get(i);
                token.deactivate();
                saveFcmPort.save(token);
                log.info("유효하지 않은 FCM 토큰 비활성화 tokenId={}, memberId={}", token.getId(), token.getMemberId());
            }
        }
    }
}
