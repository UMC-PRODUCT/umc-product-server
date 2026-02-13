package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.application.port.out.SaveFormResponsePort;
import com.umc.product.survey.application.port.out.SaveSingleAnswerPort;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.SingleAnswer;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FormResponsePersistenceAdapter implements LoadFormResponsePort, SaveFormResponsePort {

    private final FormResponseJpaRepository formResponseJpaRepository;
    private final SaveSingleAnswerPort saveSingleAnswerPort;
    private final SingleAnswerJpaRepository singleAnswerJpaRepository;

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
    public int countSubmittedByFormId(Long formId) {
        return formResponseJpaRepository.countByFormIdAndStatus(formId, FormResponseStatus.SUBMITTED);
    }

    @Override
    public List<Long> findMySelectedOptionIds(Long formId, Long memberId) {
        // 투표는 1문항/1응답 전제, "최신 SUBMITTED 응답"의 answer만 보면 됨
        List<SingleAnswer> answers = singleAnswerJpaRepository.findLatestSubmittedAnswers(
            formId, memberId, FormResponseStatus.SUBMITTED
        );

        if (answers == null || answers.isEmpty()) {
            return List.of();
        }

        // vote 구조상 사실상 1개지만 안전하게 첫 answer만 사용
        SingleAnswer a = answers.get(0);
        Map<String, Object> v = a.getValue();
        if (v == null || v.isEmpty()) {
            return List.of();
        }

        // RADIO: { selectedOptionId: 123 }
        if (a.getAnsweredAsType() == QuestionType.RADIO || a.getAnsweredAsType() == QuestionType.DROPDOWN) {
            Object id = v.get("selectedOptionId");
            if (id == null) {
                return List.of();
            }
            return List.of(toLong(id));
        }

        // CHECKBOX: { selectedOptionIds: [1,2,3] }
        if (a.getAnsweredAsType() == QuestionType.CHECKBOX) {
            Object ids = v.get("selectedOptionIds");
            if (!(ids instanceof List<?> list)) {
                return List.of();
            }
            List<Long> result = new ArrayList<>();
            for (Object o : list) {
                result.add(toLong(o));
            }
            return result;
        }

        return List.of();
    }

    private Long toLong(Object o) {
        if (o instanceof Long l) {
            return l;
        }
        if (o instanceof Integer i) {
            return i.longValue();
        }
        if (o instanceof String s) {
            return Long.parseLong(s);
        }
        throw new IllegalArgumentException("Cannot convert to Long: " + o);
    }
}
