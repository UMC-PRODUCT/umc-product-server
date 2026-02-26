package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentInterviewPreferenceCommand;
import com.umc.product.recruitment.application.port.in.command.dto.UpdateRecruitmentInterviewPreferenceInfo;

public interface UpdateRecruitmentInterviewPreferenceUseCase {
    UpdateRecruitmentInterviewPreferenceInfo update(UpdateRecruitmentInterviewPreferenceCommand command);
}
