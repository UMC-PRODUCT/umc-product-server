package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.ChangeInquiryTeamCommand;

public interface ChangeInquiryTeamUseCase {

    void changeTeam(ChangeInquiryTeamCommand command);
}
