package com.umc.product.survey.application.service.query;

import com.umc.product.survey.application.port.in.query.GetVoteUseCase;
import com.umc.product.survey.application.port.in.query.dto.VoteInfo;
import com.umc.product.survey.application.port.in.query.dto.VoteInfo.VoteOptionInfo;
import com.umc.product.survey.application.port.out.*;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.QuestionOption;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VoteQueryService implements GetVoteUseCase {

    private final LoadFormPort loadFormPort;
    private final LoadAnswerPort loadAnswerPort;
    private final LoadFormSectionPort loadFormSectionPort;
    private final LoadQuestionPort loadQuestionPort;
    private final LoadQuestionOptionPort loadQuestionOptionPort;

    @Override
    public VoteInfo getVoteInfo(Long formId, Long memberId) {
        Form form = loadFormPort.findById(formId)
            .orElseThrow(() -> new IllegalArgumentException("해당 투표가 존재하지 않습니다. ID: " + formId));

        // 1. 투표 통계 데이터 로드
        Map<Long, Long> counts = loadAnswerPort.countVotesByOptionId(formId);
        int totalParticipants = loadAnswerPort.countTotalParticipants(formId);
        List<Long> mySelections = loadAnswerPort.findSelectedOptionIdsByMember(formId, memberId);

        // 2. 투표 구조 로드 (섹션 -> 질문 -> 옵션)
        // 공식적으로는 1섹션 1질문 구조를 전제로 하지만, 확장성을 고려하여 조회
        FormSection section = loadFormSectionPort.findAllByFormId(formId).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("투표 섹션이 존재하지 않습니다. ID: " + formId));

        Question question = loadQuestionPort.findAllByFormSectionIdIn(Set.of(section.getId())).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("투표 질문이 존재하지 않습니다. SectionID: " + section.getId()));

        boolean allowMultipleChoice = (question.getType() == QuestionType.CHECKBOX);
        List<QuestionOption> questionOptions = loadQuestionOptionPort.findAllByQuestionId(question.getId());

        // 3. 옵션별 투표자 ID 목록 로드 (익명인 경우 빈 맵)
        Map<Long, List<Long>> selectedMemberIdsByOptionId = form.isAnonymous()
            ? Map.of()
            : loadAnswerPort.findSelectedMemberIdsByOptionId(formId);

        // 4. 옵션 정보 및 득표율 계산
        List<VoteOptionInfo> optionInfos = questionOptions.stream()
            .map(opt -> {
                int voteCount = counts.getOrDefault(opt.getId(), 0L).intValue();
                BigDecimal voteRate = calculateVoteRate(voteCount, totalParticipants);
                List<Long> selectedMemberIds = selectedMemberIdsByOptionId.getOrDefault(opt.getId(), List.of());

                return new VoteOptionInfo(
                    opt.getId(),
                    opt.getContent(),
                    voteCount,
                    voteRate,
                    selectedMemberIds
                );
            })
            .toList();

        return new VoteInfo(
            form.getId(),
            form.getTitle(),
            form.isAnonymous(),
            allowMultipleChoice,
            totalParticipants,
            mySelections,
            optionInfos
        );
    }

    @Override
    public Long getPrimaryQuestionId(Long voteId) {
        List<Question> questions = loadQuestionPort.findAllByFormId(voteId);
        if (questions.isEmpty()) {
            throw new SurveyDomainException(SurveyErrorCode.INVALID_VOTE_FORM_STRUCTURE);
        }
        return questions.get(0).getId();
    }

    private BigDecimal calculateVoteRate(int voteCount, int totalParticipants) {
        if (totalParticipants == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(voteCount)
            .divide(BigDecimal.valueOf(totalParticipants), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
