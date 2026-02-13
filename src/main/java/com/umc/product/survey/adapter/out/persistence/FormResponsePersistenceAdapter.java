package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.application.port.out.SaveFormResponsePort;
import com.umc.product.survey.application.port.out.SaveSingleAnswerPort;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FormResponsePersistenceAdapter implements LoadFormResponsePort, SaveFormResponsePort {

    private final FormResponseJpaRepository formResponseJpaRepository;
    private final SaveSingleAnswerPort saveSingleAnswerPort;

    @Override
    public Optional<FormResponse> findById(Long formResponseId) {
        return formResponseJpaRepository.findById(formResponseId);
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
    public void deleteDraftsByFormId(Long formId) {
        List<Long> draftIds = formResponseJpaRepository.findIdsByFormIdAndStatus(formId, FormResponseStatus.DRAFT);
        if (draftIds.isEmpty()) {
            return;
        }

        saveSingleAnswerPort.deleteAllByFormResponseIds(draftIds);

        formResponseJpaRepository.deleteAllByIdInBatch(draftIds);
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
}
