package com.umc.product.test.application.service.rehearsal;

import java.util.List;
import java.util.Objects;

import com.umc.product.project.application.port.in.command.dto.UpdateProjectApplicationDraftCommand.AnswerEntry;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo.OptionInfo;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo.QuestionInfo;
import com.umc.product.test.domain.rehearsal.RoundSnapshot;

/**
 * 봇 지원서 답변. 주관식은 {@code [봇]}으로 시작하는 짧은 문장, 객관식은 첫 선택지다. 파일·일정 질문은 답하지 않는다.
 */
final class BotAnswers {

    private BotAnswers() {
    }

    static List<AnswerEntry> of(ApplicationFormInfo form) {
        return form.sections().stream()
            .flatMap(section -> section.questions().stream())
            .map(BotAnswers::answer)
            .filter(Objects::nonNull)
            .toList();
    }

    private static AnswerEntry answer(QuestionInfo question) {
        return switch (question.type()) {
            case SHORT_TEXT, LONG_TEXT, PORTFOLIO -> AnswerEntry.builder()
                .questionId(question.questionId())
                .textValue(RoundSnapshot.BOT_ANSWER_PREFIX + " " + question.title() + "에 대한 답변입니다.")
                .selectedOptionIds(List.of()).fileIds(List.of())
                .build();
            case RADIO, DROPDOWN, CHECKBOX -> question.options().stream()
                .min((left, right) -> Long.compare(left.orderNo(), right.orderNo()))
                .map(OptionInfo::optionId)
                .map(optionId -> AnswerEntry.builder()
                    .questionId(question.questionId())
                    .selectedOptionIds(List.of(optionId)).fileIds(List.of())
                    .build())
                .orElse(null);
            case FILE, SCHEDULE -> null;
        };
    }
}
