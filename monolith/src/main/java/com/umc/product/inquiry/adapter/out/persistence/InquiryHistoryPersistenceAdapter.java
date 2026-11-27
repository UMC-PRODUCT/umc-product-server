package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.port.out.LoadInquiryHistoryPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryHistoryPort;
import com.umc.product.inquiry.domain.InquiryHistory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryHistoryPersistenceAdapter implements LoadInquiryHistoryPort, SaveInquiryHistoryPort {

    private final InquiryHistoryJpaRepository repository;

    @Override
    public List<InquiryHistory> listByInquiryId(Long inquiryId) {
        return repository.findAllByInquiryIdOrderByIdAsc(inquiryId);
    }

    @Override
    public InquiryHistory save(InquiryHistory history) {
        return repository.save(history);
    }
}
