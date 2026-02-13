package com.umc.product.survey.application.service;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.survey.application.port.in.command.CreateVoteUseCase;
import com.umc.product.survey.application.port.in.command.DeleteVoteUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateVoteCommand;
import com.umc.product.survey.application.port.in.command.dto.DeleteVoteCommand;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class VoteService implements CreateVoteUseCase, DeleteVoteUseCase {

    private final SaveFormPort saveFormPort;
    private final LoadFormPort loadFormPort;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

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

        saveFormPort.deleteById(form.getId());
    }
}
