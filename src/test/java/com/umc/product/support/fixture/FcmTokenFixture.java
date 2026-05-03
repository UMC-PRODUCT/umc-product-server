package com.umc.product.support.fixture;

import com.umc.product.notification.application.port.out.SaveFcmPort;
import com.umc.product.notification.domain.FcmToken;
import org.springframework.stereotype.Component;

@Component
public class FcmTokenFixture extends FixtureSupport {

    private final SaveFcmPort saveFcmPort;

    public FcmTokenFixture(SaveFcmPort saveFcmPort) {
        this.saveFcmPort = saveFcmPort;
    }

    public FcmToken FCM_토큰(Long memberId, String token) {
        FcmToken fcmToken = FcmToken.create(
            memberId,
            valueOrFixture(token, "fcm-token", 100)
        );
        saveFcmPort.save(fcmToken);
        return fcmToken;
    }
}
