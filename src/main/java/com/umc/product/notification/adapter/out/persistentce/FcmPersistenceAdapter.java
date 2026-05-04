package com.umc.product.notification.adapter.out.persistentce;

import com.umc.product.notification.application.port.out.LoadFcmPort;
import com.umc.product.notification.application.port.out.SaveFcmPort;
import com.umc.product.notification.domain.FcmToken;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FcmPersistenceAdapter implements LoadFcmPort, SaveFcmPort {

    private final FcmJpaRepository fcmJpaRepository;

    @Override
    public Optional<FcmToken> findByMemberIdAndToken(Long memberId, String fcmToken) {
        return fcmJpaRepository.findByMemberIdAndFcmToken(memberId, fcmToken);
    }

    @Override
    public List<FcmToken> findAllActiveByMemberId(Long memberId) {
        return fcmJpaRepository.findAllByMemberIdAndIsActiveTrue(memberId);
    }

    @Override
    public List<FcmToken> findAllActiveByMemberIds(List<Long> memberIds) {
        return fcmJpaRepository.findAllByMemberIdInAndIsActiveTrue(memberIds);
    }

    @Override
    public void save(FcmToken fcmToken) {
        fcmJpaRepository.save(fcmToken);
    }
}
