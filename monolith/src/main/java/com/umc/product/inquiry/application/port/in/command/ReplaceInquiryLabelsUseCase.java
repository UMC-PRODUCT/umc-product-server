package com.umc.product.inquiry.application.port.in.command;

import com.umc.product.inquiry.application.port.in.command.dto.ReplaceInquiryLabelsCommand;

public interface ReplaceInquiryLabelsUseCase {

    /** 문의의 라벨을 주어진 집합으로 통째로 바꾼다. */
    void replaceLabels(ReplaceInquiryLabelsCommand command);
}
