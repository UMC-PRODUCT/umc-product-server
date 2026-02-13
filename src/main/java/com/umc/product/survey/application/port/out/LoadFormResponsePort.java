package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import java.util.List;
import java.util.Optional;

public interface LoadFormResponsePort {

    Optional<FormResponse> findById(Long formResponseId);

    Optional<FormResponse> findDraftByFormIdAndRespondentMemberId(Long formId, Long respondentMemberId);

    List<FormResponse> findAllDraftByRespondentMemberId(Long respondentMemberId);

    List<Long> findDraftIdsByFormId(Long formId);

    boolean existsByFormIdAndMemberId(Long formId, Long memberId);

    List<Long> findIdsByFormIdAndStatus(Long formId, FormResponseStatus status);

    int countSubmittedByFormId(Long formId);

    List<Long> findMySelectedOptionIds(Long formId, Long memberId);
}
