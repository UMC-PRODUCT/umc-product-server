package com.umc.product.test.application.port.in.command.dto;

import java.util.Arrays;

import com.umc.product.common.domain.exception.CommonException;
import com.umc.product.global.exception.constant.CommonErrorCode;

/**
 * QA 시딩 단계. 선언 순서가 전체 시딩의 실행 순서이고, path가 {@code /test/seed/qa/{step}} 경로다.
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

    /**
     * 계정 단계가 실패하면 뒤 단계는 계정을 찾지 못하므로 실행하지 않는다.
     */
    public boolean blocksOthersOnFailure() {
        return this == ACCOUNTS;
    }

    public static QaSeedStep fromPath(String path) {
        return Arrays.stream(values()).filter(step -> step.path.equals(path)).findFirst()
            .orElseThrow(() -> new CommonException(CommonErrorCode.BAD_REQUEST, "알 수 없는 QA 시딩 단계입니다: " + path));
    }
}
