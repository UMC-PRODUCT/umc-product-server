package com.umc.product.inquiry.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.inquiry.domain.Inquiry;

import jakarta.persistence.LockModeType;

public interface InquiryJpaRepository extends JpaRepository<Inquiry, Long> {

    Optional<Inquiry> findByChatRoomId(Long chatRoomId);

    Optional<Inquiry> findByAuthorMemberIdAndClientRequestId(Long authorMemberId, UUID clientRequestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select inquiry from Inquiry inquiry where inquiry.id = :inquiryId")
    Optional<Inquiry> findByIdForUpdate(@Param("inquiryId") Long inquiryId);
}
