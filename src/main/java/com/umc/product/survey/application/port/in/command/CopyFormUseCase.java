package com.umc.product.survey.application.port.in.command;

public interface CopyFormUseCase {
    Long copyForm(Long sourceFormId, Long memberId, String newRecruitmentTitle);
}
