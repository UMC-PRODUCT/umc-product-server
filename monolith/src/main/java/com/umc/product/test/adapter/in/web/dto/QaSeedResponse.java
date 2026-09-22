package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.test.application.port.in.command.dto.QaSeedResult;

public record QaSeedResponse(int createdMemberCount) {
    public static QaSeedResponse from(QaSeedResult result) {
        return new QaSeedResponse(result.createdMemberCount());
    }
}
