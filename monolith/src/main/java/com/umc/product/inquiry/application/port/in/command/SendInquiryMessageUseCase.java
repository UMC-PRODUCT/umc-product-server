package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.SendInquiryMessageCommand;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;

public interface SendInquiryMessageUseCase {

    InquiryMessageInfo send(SendInquiryMessageCommand command);
}
