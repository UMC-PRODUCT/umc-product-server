package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

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
    public Optional<Inquiry> findByAuthorMemberIdAndClientRequestId(Long authorMemberId, UUID clientRequestId) {
        return inquiryJpaRepository.findByAuthorMemberIdAndClientRequestId(authorMemberId, clientRequestId);
    }

    @Override
    public List<Inquiry> listByAuthor(Long authorMemberId, Set<InquiryStatus> statuses, Cursor cursor, int limit) {
        return inquiryQueryRepository.listByAuthor(authorMemberId, statuses, cursor, limit);
    }

    @Override
    public Inquiry save(Inquiry inquiry) {
        try {
            return inquiryJpaRepository.saveAndFlush(inquiry);
        } catch (DataIntegrityViolationException exception) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_REQUEST_CONFLICT, exception);
        }
    }
}
