package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.ReplaceInquiryLabelsCommand;

public interface ReplaceInquiryLabelsUseCase {

    void replaceLabels(ReplaceInquiryLabelsCommand command);
}
