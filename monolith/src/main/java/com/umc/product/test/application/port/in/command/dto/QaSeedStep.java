package com.umc.product.test.application.port.in.command.dto;

import java.util.Arrays;

import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;

/**
 * 선언 순서가 전체 시딩의 실행 순서다.
 */
public enum QaSeedStep {
    ACCOUNTS("accounts"),
    LEARNING("learning"),
    ATTENDANCE("attendance"),
    NOTICES("notices"),
    POINTS("points"),
    CHAT("chat"),
    PROJECT_TRANSFERS("project-transfers");

    private final String path;

    QaSeedStep(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public boolean blocksOthersOnFailure() {
        return this == ACCOUNTS;
    }

    public static QaSeedStep fromPath(String path) {
        return Arrays.stream(values()).filter(step -> step.path.equals(path)).findFirst()
            .orElseThrow(() -> new CommonException(CommonErrorCode.BAD_REQUEST, "알 수 없는 QA 시딩 단계입니다: " + path));
    }
}
