package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.umc.product.global.persistence.ConstraintViolationInspector;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryPersistenceAdapter implements LoadInquiryPort, SaveInquiryPort {

    private static final String AUTHOR_CLIENT_REQUEST_CONSTRAINT = "uq_inquiry_author_client_request";

    private final InquiryJpaRepository inquiryJpaRepository;
    private final InquiryQueryRepository inquiryQueryRepository;

    @Override
    public Optional<Inquiry> findById(Long inquiryId) {
        return inquiryJpaRepository.findById(inquiryId);
    }

    @Override
    public Optional<Inquiry> findByIdForUpdate(Long inquiryId) {
        return inquiryJpaRepository.findByIdForUpdate(inquiryId);
    }

    @Override
    public Optional<Inquiry> findByChatRoomId(Long chatRoomId) {
        return inquiryJpaRepository.findByChatRoomId(chatRoomId);
    }

    @Override
    public Optional<Inquiry> findByAuthorMemberIdAndClientRequestId(Long authorMemberId, UUID clientRequestId) {
        return inquiryJpaRepository.findByAuthorMemberIdAndClientRequestId(authorMemberId, clientRequestId);
    }

    @Override
    public List<Inquiry> listByAuthor(Long authorMemberId, Set<InquiryStatus> statuses, Cursor cursor, int limit) {
        return inquiryQueryRepository.listByAuthor(authorMemberId, statuses, cursor, limit);
    }

    @Override
    public List<Inquiry> searchForStaff(StaffSearch search, Cursor cursor, int limit) {
        return inquiryQueryRepository.searchForStaff(search, cursor, limit);
    }

    @Override
    public long countForStaff(StaffSearch search) {
        return inquiryQueryRepository.countForStaff(search);
    }

    @Override
    public Inquiry create(Inquiry inquiry) {
        try {
            return inquiryJpaRepository.saveAndFlush(inquiry);
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintViolationInspector.matches(exception, AUTHOR_CLIENT_REQUEST_CONSTRAINT)) {
                throw new InquiryDomainException(InquiryErrorCode.INQUIRY_REQUEST_CONFLICT, exception);
            }
            throw exception;
        }
    }

    @Override
    public Inquiry save(Inquiry inquiry) {
        return inquiryJpaRepository.save(inquiry);
    }
}
