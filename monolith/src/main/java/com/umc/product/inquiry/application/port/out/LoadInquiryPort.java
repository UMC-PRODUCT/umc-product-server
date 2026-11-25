package com.umc.product.inquiry.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryStaffTab;
import com.umc.product.inquiry.domain.enums.InquiryStatus;

public interface LoadInquiryPort {

    Optional<Inquiry> findById(Long inquiryId);

    Optional<Inquiry> findByIdForUpdate(Long inquiryId);

    Optional<Inquiry> findByChatRoomId(Long chatRoomId);

    Optional<Inquiry> findByAuthorMemberIdAndClientRequestId(Long authorMemberId, UUID clientRequestId);

    /**
     * 작성자의 문의를 마지막 메시지 최신순으로 조회한다. {@code cursor}가 있으면 그 문의보다 뒤에 오는 문의만 돌려준다.
     */
    List<Inquiry> listByAuthor(Long authorMemberId, Set<InquiryStatus> statuses, Cursor cursor, int limit);

    /**
     * 운영 목록을 마지막 메시지 최신순으로 조회한다.
     */
    List<Inquiry> searchForStaff(StaffSearch search, Cursor cursor, int limit);

    long countForStaff(StaffSearch search);

    record Cursor(Instant lastMessageAt, Long inquiryId) {
    }

    /**
     * @param staffMemberIds 지금 {@code UMC_PRODUCT} 운영진. 담당자가 이 안에 없으면 미배정으로 본다.
     * @param labelIds       하나라도 붙은 문의만 고른다. 비면 거르지 않는다.
     */
    record StaffSearch(
        InquiryStaffTab tab,
        Long viewerMemberId,
        Set<Long> staffMemberIds,
        Set<InquiryStatus> statuses,
        Set<Long> categoryIds,
        Set<Long> labelIds
    ) {
        public StaffSearch withTab(InquiryStaffTab otherTab) {
            return new StaffSearch(otherTab, viewerMemberId, staffMemberIds, statuses, categoryIds, labelIds);
        }
    }
}
