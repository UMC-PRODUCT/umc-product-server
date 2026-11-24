package com.umc.product.inquiry.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryStatus;

public interface LoadInquiryPort {

    Optional<Inquiry> findById(Long inquiryId);

    Optional<Inquiry> findByIdForUpdate(Long inquiryId);

    Optional<Inquiry> findByAuthorMemberIdAndClientRequestId(Long authorMemberId, UUID clientRequestId);

    /**
     * 작성자의 문의를 마지막 메시지 최신순으로 조회한다. {@code cursor}가 있으면 그 문의보다 뒤에 오는 문의만 돌려준다.
     */
    List<Inquiry> listByAuthor(Long authorMemberId, Set<InquiryStatus> statuses, Cursor cursor, int limit);

    record Cursor(Instant lastMessageAt, Long inquiryId) {
    }
}
