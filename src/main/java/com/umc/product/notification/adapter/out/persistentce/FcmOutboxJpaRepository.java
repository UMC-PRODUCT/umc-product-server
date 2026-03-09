package com.umc.product.notification.adapter.out.persistentce;

import com.umc.product.notification.domain.FcmOutbox;
import com.umc.product.notification.domain.FcmOutboxStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmOutboxJpaRepository extends JpaRepository<FcmOutbox, Long> {
    List<FcmOutbox> findByStatus(FcmOutboxStatus status);
}
