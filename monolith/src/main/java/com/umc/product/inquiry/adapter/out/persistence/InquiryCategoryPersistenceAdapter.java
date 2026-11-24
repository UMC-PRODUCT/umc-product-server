package com.umc.product.inquiry.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.port.out.LoadInquiryCategoryPort;
import com.umc.product.inquiry.domain.InquiryCategory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryCategoryPersistenceAdapter implements LoadInquiryCategoryPort {

    private final InquiryCategoryJpaRepository inquiryCategoryJpaRepository;

    @Override
    public Optional<InquiryCategory> findById(Long categoryId) {
        return inquiryCategoryJpaRepository.findById(categoryId);
    }

    @Override
    public List<InquiryCategory> listActive() {
        return inquiryCategoryJpaRepository.findAllByActiveTrueOrderByDisplayOrderAscIdAsc();
    }

    @Override
    public List<InquiryCategory> listByIds(Collection<Long> categoryIds) {
        return inquiryCategoryJpaRepository.findAllById(categoryIds);
    }
}
