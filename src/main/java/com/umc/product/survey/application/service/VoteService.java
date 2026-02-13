package com.umc.product.survey.application.service;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.survey.application.port.in.command.CreateVoteUseCase;
import com.umc.product.survey.application.port.in.command.DeleteVoteUseCase;
import com.umc.product.survey.application.port.in.command.SubmitVoteResponseUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateVoteCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteVoteCommand;
import com.umc.product.survey.application.port.in.command.dto.SubmitVoteResponseCommand;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.application.port.out.SaveFormResponsePort;
import com.umc.product.survey.application.port.out.SaveSingleAnswerPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.QuestionOption;
import com.umc.product.survey.domain.enums.FormOpenStatus;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class VoteService implements CreateVoteUseCase, DeleteVoteUseCase, SubmitVoteResponseUseCase {

    private final SaveFormPort saveFormPort;
    private final LoadFormPort loadFormPort;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final LoadFormResponsePort loadFormResponsePort;
    private final SaveFormResponsePort saveFormResponsePort;
    private final SaveSingleAnswerPort saveSingleAnswerPort;

    @Override
    public Long create(CreateVoteCommand command) {
        validate(command);

        QuestionType qType = command.allowMultipleChoice()
                ? QuestionType.CHECKBOX
                : QuestionType.RADIO;

        Form form = Form.createPublished(
                command.createdMemberId(),
                command.title()
        );

        form.setVotePolicy(command.isAnonymous(), command.startsAt(), command.endsAtExclusive());

        // 섹션/질문/옵션 조립
        form.appendSingleQuestion(
                command.title(), // 투표 제목 그대로 questionText로 사용
                qType,
                command.options()
        );

        return saveFormPort.save(form).getId();
    }

    private void validate(CreateVoteCommand cmd) {
        List<String> options = cmd.options();
        if (options == null || options.size() < 2 || options.size() > 5) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_OPTION_COUNT);
        }
        if (options.stream().anyMatch(s -> s == null || s.trim().isEmpty())) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_OPTION_CONTENT);
        }

        Instant startsAt = cmd.startsAt();
        Instant endsAtExclusive = cmd.endsAtExclusive();

        if (startsAt == null || endsAtExclusive == null) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_FORM_ACTIVE_PERIOD);
        }

        // 기본 기간 검증: end > start
        if (!endsAtExclusive.isAfter(startsAt)) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_FORM_ACTIVE_PERIOD);
        }

        // 시작일: 오늘부터 선택 가능 (KST 기준 오늘 00:00 이상)
        Instant todayStartKst = LocalDate.now(KST).atStartOfDay(KST).toInstant();
        if (startsAt.isBefore(todayStartKst)) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_START_DATE);
        }

        // 마감일: 시작일 하루 뒤부터 선택 가능
        // startsAt = startDate 00:00(KST)
        // endsAtExclusive = (endDate + 1) 00:00(KST)
        // endDate >= startDate + 1  <=>  endsAtExclusive >= startsAt + 2 days
        Instant minEndsAtExclusive = startsAt.plus(2, ChronoUnit.DAYS);
        if (endsAtExclusive.isBefore(minEndsAtExclusive)) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_END_DATE);
        }
    }

    @Override
    public void delete(DeleteVoteCommand cmd) {
        Form form = loadFormPort.findById(cmd.voteId())
                .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND));

        // todo: 권한 검증 추가

        List<Long> draftIds = loadFormResponsePort.findDraftIdsByFormId(form.getId());
        if (draftIds != null && !draftIds.isEmpty()) {
            saveSingleAnswerPort.deleteAllByFormResponseIds(draftIds);
            saveFormResponsePort.deleteAllByIds(draftIds);
        }

        FormResponseStatus st = FormResponseStatus.SUBMITTED;
        List<Long> responseIds = loadFormResponsePort.findIdsByFormIdAndStatus(form.getId(), st);
        if (responseIds != null && !responseIds.isEmpty()) {
            saveSingleAnswerPort.deleteAllByFormResponseIds(responseIds);
            saveFormResponsePort.deleteByFormIdAndStatus(form.getId(), st);
        }

        saveFormPort.deleteById(form.getId());
    }

    @Override
    public void submit(SubmitVoteResponseCommand cmd) {
        Instant now = Instant.now();

        Form form = loadFormPort.findById(cmd.voteId())
                .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_FOUND));

        // 1) 기간 체크
        FormOpenStatus openStatus = form.getOpenStatus(now);
        if (openStatus == FormOpenStatus.NOT_STARTED) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.VOTE_NOT_STARTED);
        }
        if (openStatus == FormOpenStatus.CLOSED) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.VOTE_CLOSED);
        }

        // 2) 중복 투표 체크
        if (loadFormResponsePort.existsByFormIdAndMemberId(form.getId(), cmd.memberId())) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.VOTE_ALREADY_RESPONDED);
        }

        // 3) 투표 구조에서 Question 1개 꺼내기
        Question question = extractSingleQuestion(form);

        // 4) 선택 검증
        List<Long> optionIds = cmd.optionIds();
        if (optionIds == null || optionIds.isEmpty()) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_SELECTION);
        }

        // 중복 제거
        Set<Long> uniqueOptionIds = new LinkedHashSet<>(optionIds);

        if (question.getType() == QuestionType.RADIO) {
            if (uniqueOptionIds.size() != 1) {
                throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_SELECTION);
            }
        } else if (question.getType() == QuestionType.CHECKBOX) {
            // 추가 검증 x
        } else {
            // 투표 질문은 RADIO/CHECKBOX만 와야 함
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_QUESTION_TYPE);
        }

        // 5) optionIds가 이 question의 옵션인지 검증
        Set<Long> allowedOptionIds = new HashSet<>();
        for (QuestionOption opt : question.getOptions()) {
            allowedOptionIds.add(opt.getId());
        }
        if (!allowedOptionIds.containsAll(new HashSet<>(uniqueOptionIds))) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_SELECTION);
        }

        // 6) 응답 저장
        FormResponse formResponse = FormResponse.createVoteResponse(
                form,
                cmd.memberId(),
                question,
                uniqueOptionIds.stream().toList(),
                now
        );

        saveFormResponsePort.save(formResponse);
    }

    private Question extractSingleQuestion(Form form) {
        if (form.getSections().isEmpty()) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_FORM_STRUCTURE);
        }
        // 섹션 1개 전제
        var section = form.getSections().iterator().next();
        if (section.getQuestions().isEmpty()) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_FORM_STRUCTURE);
        }
        return section.getQuestions().iterator().next();
    }
}
