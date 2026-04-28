package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.application.port.out.SaveFormResponsePort;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FormResponsePersistenceAdapter implements LoadFormResponsePort, SaveFormResponsePort {

    private final FormResponseJpaRepository formResponseJpaRepository;
    private final FormResponseQueryRepository formResponseQueryRepository;

    @Override
    public Optional<FormResponse> findById(Long formResponseId) {
        return formResponseJpaRepository.findById(formResponseId);
    }

    @Override
    public List<FormResponse> listByFormId(Long formId) {
        return formResponseQueryRepository.findAllByFormId(formId);
    }

    @Override
    public List<FormResponse> listSubmittedByFormId(Long formId) {
        return formResponseQueryRepository.findAllSubmittedByFormId(formId);
    }

    @Override
    public Optional<FormResponse> findDraftByFormIdAndRespondentMemberId(Long formId, Long respondentMemberId) {
        return formResponseJpaRepository.findFirstByForm_IdAndRespondentMemberIdAndStatusOrderByIdDesc(
            formId, respondentMemberId, FormResponseStatus.DRAFT
        );
    }

    @Override
    public FormResponse save(FormResponse formResponse) {
        return formResponseJpaRepository.save(formResponse);
    }

    @Override
    public void deleteById(Long formResponseId) {
        formResponseJpaRepository.deleteById(formResponseId);
    }

    @Override
    public List<FormResponse> findAllDraftByRespondentMemberId(Long respondentMemberId) {
        return formResponseJpaRepository.findByRespondentMemberIdAndStatus(
            respondentMemberId, FormResponseStatus.DRAFT
        );
    }

    @Override
    public List<Long> findDraftIdsByFormId(Long formId) {
        return formResponseJpaRepository.findIdsByFormIdAndStatus(formId, FormResponseStatus.DRAFT);
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        formResponseJpaRepository.deleteAllByIdInBatch(ids);
    }

    @Override
    public boolean existsByFormIdAndMemberId(Long formId, Long memberId) {
        return formResponseJpaRepository.existsByForm_IdAndRespondentMemberId(formId, memberId);
    }

    @Override
    @Transactional
    public int deleteByFormIdAndStatus(Long formId, FormResponseStatus status) {
        return formResponseJpaRepository.deleteByFormIdAndStatus(formId, status);
    }

    @Override
    public List<Long> findIdsByFormIdAndStatus(Long formId, FormResponseStatus status) {
        return formResponseJpaRepository.findIdsByFormIdAndStatus(formId, status);
    }

    @Override
    public long countSubmittedByFormId(Long formId) {
        return formResponseJpaRepository.countByFormIdAndStatus(formId, FormResponseStatus.SUBMITTED);
    }

    @Override
    public Optional<FormResponse> findSubmittedByFormIdAndRespondentMemberId(Long formId, Long respondentMemberId) {
        return formResponseJpaRepository.findFirstByForm_IdAndRespondentMemberIdAndStatusOrderByIdDesc(
            formId, respondentMemberId, FormResponseStatus.SUBMITTED
        );
    }

    @Override
    @Transactional
    public void deleteByFormId(Long formId) {
        formResponseJpaRepository.deleteByFormId(formId);
    }
}
