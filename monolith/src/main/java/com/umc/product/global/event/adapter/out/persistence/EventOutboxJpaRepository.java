package com.umc.product.global.event.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.global.event.domain.EventOutbox;

public interface EventOutboxJpaRepository extends JpaRepository<EventOutbox, Long> {
}
