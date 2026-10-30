package com.umc.product.report.adapter.in.web.discord;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;

/**
 * 사유 입력창에 보여 줄 사유 항목과 이름입니다. 이름은 신고 처리 정책 문서의 표현을 따릅니다.
 */
final class DiscordReportReasons {

    private DiscordReportReasons() {
    }

    /**
     * 조치에 쓸 수 있는 사유를 정의한 순서대로 돌려줍니다.
     */
    static List<ReportActionReason> of(ReportActionType actionType) {
        return Arrays.stream(ReportActionReason.values())
            .filter(reason -> reason.actionType() == actionType)
            .toList();
    }

    /**
     * 입력창에서 고른 값을 사유로 읽습니다. 모르는 값이면 비어 있는 값을 돌려줍니다.
     */
    static Optional<ReportActionReason> parse(String value) {
        return Arrays.stream(ReportActionReason.values())
            .filter(reason -> reason.name().equals(value))
            .findFirst();
    }

    static String label(ReportActionReason reason) {
        return switch (reason) {
            case SPAM -> "스팸·광고";
            case ABUSE -> "욕설·비방·혐오 표현";
            case INAPPROPRIATE -> "부적절하거나 불쾌한 내용";
            case PRIVACY -> "개인정보 노출";
            case OTHER_VIOLATION -> "기타 위반";
            case NOT_VIOLATION -> "운영 정책 위반 아님";
            case CONTENT_MISMATCH -> "신고 내용과 확인 결과가 일치하지 않음";
            case UNVERIFIABLE -> "신고 내용 확인 어려움";
            case DUPLICATE -> "중복 신고 또는 이미 처리된 신고";
            case OTHER_REJECTION -> "기타";
        };
    }
}
