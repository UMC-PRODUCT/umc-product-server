package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.application.port.in.command.dto.UpsertRecruitmentFormQuestionsCommand.Item;
import com.umc.product.recruitment.domain.Recruitment;
import java.util.List;

public interface SaveRecruitmentPort {
    Recruitment save(Recruitment recruitment);

    void deleteById(Long recruitmentId);

    void upsertQuestions(Long formId, List<Item> items);
}
