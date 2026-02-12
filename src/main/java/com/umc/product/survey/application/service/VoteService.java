package com.umc.product.survey.application.service;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.survey.application.port.in.command.CreateVoteUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateVoteCommand;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.enums.QuestionType;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class VoteService implements CreateVoteUseCase {

    private final SaveFormPort saveFormPort;

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
        if (cmd.startsAt() != null && cmd.endsAtExclusive() != null && !cmd.endsAtExclusive().isAfter(cmd.startsAt())) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_FORM_ACTIVE_PERIOD);
        }
        if (options.stream().anyMatch(s -> s == null || s.trim().isEmpty())) {
            throw new BusinessException(Domain.SURVEY, SurveyErrorCode.INVALID_VOTE_OPTION_CONTENT);
        }
    }
}
