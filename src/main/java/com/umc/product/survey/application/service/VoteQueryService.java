package com.umc.product.survey.application.service;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.survey.application.port.in.query.GetVoteDetailUseCase;
import com.umc.product.survey.application.port.in.query.dto.GetVoteDetailsQuery;
import com.umc.product.survey.application.port.in.query.dto.VoteInfo;
import com.umc.product.survey.application.port.in.query.dto.VoteInfo.VoteOptionInfo;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.application.port.out.LoadSingleAnswerPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.QuestionOption;
import com.umc.product.survey.domain.enums.FormOpenStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VoteQueryService implements GetVoteDetailUseCase {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final LoadFormPort loadFormPort;
    private final LoadFormResponsePort loadFormResponsePort;
    private final LoadSingleAnswerPort loadSingleAnswerPort;

    @Override
    public VoteInfo get(GetVoteDetailsQuery query) {
        Long voteId = query.voteId();
        Long memberId = query.memberId();

        Instant now = Instant.now();

        Form form = loadFormPort.findById(voteId)
            .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND));

        // 투표는 "섹션 1 / 질문 1" 전제
        Question question = extractSingleQuestion(form);

        boolean allowMultipleChoice = (question.getType() == QuestionType.CHECKBOX);
        FormOpenStatus openStatus = form.getOpenStatus(now);

        // KST 날짜
        Instant startsAt = form.getStartsAt();
        Instant endsAtExclusive = form.getEndsAtExclusive();

        LocalDate startDateKst = (startsAt == null) ? null : startsAt.atZone(KST).toLocalDate();
        LocalDate endDateKst =
            (endsAtExclusive == null) ? null : endsAtExclusive.atZone(KST).toLocalDate().minusDays(1);

        // 1) 총 참여자 수(=SUBMITTED 응답 수)
        int totalParticipants = loadFormResponsePort.countSubmittedByFormId(voteId);

        // 2) 옵션별 득표수 (optionId -> count)
        Map<Long, Integer> optionCounts = loadSingleAnswerPort.countVotesByOptionId(voteId);

        // 3) 옵션 DTO 구성 (옵션 순서 유지)
        List<VoteInfo.VoteOptionInfo> options = new ArrayList<>();
        for (QuestionOption opt : question.getOptions()) {
            Long optionId = opt.getId();
            int voteCount = optionCounts.getOrDefault(optionId, 0);

            BigDecimal voteRate = calcRate(voteCount, totalParticipants);

            options.add(new VoteOptionInfo(
                optionId,
                opt.getContent(),
                voteCount,
                voteRate
            ));
        }

        // 4) 내 응답 (미투표면 [])
        List<Long> mySelectedOptionIds = loadFormResponsePort.findMySelectedOptionIds(voteId, memberId);
        if (mySelectedOptionIds == null) {
            mySelectedOptionIds = List.of();
        } else {
            mySelectedOptionIds = List.copyOf(new LinkedHashSet<>(mySelectedOptionIds)); // 중복 방지
        }

        return new VoteInfo(
            voteId,
            form.getTitle(),
            form.isAnonymous(),
            allowMultipleChoice,
            openStatus,
            startsAt,
            endsAtExclusive,
            startDateKst,
            endDateKst,
            totalParticipants,
            options,
            mySelectedOptionIds
        );
    }

    private BigDecimal calcRate(int voteCount, int totalParticipants) {
        if (totalParticipants <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(voteCount)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(totalParticipants), 1, RoundingMode.HALF_UP);
    }

    private Question extractSingleQuestion(Form form) {
        if (form.getSections() == null || form.getSections().isEmpty()) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_FORM_STRUCTURE);
        }
        var section = form.getSections().iterator().next();
        if (section.getQuestions() == null || section.getQuestions().isEmpty()) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_FORM_STRUCTURE);
        }

        Question q = section.getQuestions().iterator().next();

        // 투표 질문 타입 가드
        if (q.getType() != QuestionType.RADIO && q.getType() != QuestionType.CHECKBOX) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_QUESTION_TYPE);
        }
        return q;
    }
}
