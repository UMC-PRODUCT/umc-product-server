package com.umc.product.test.domain.roster;

import java.util.List;

/**
 * 고정 데이터로 두는 매칭 지부 이동 4건. 프로젝트 초기화로 지우지 않는다.
 */
public final class QaTransfers {
    public static final List<QaTransfer> ALL = List.of(
        new QaTransfer(RosterKey.of("dongyang", 4), "캥거루"),
        new QaTransfer(RosterKey.of("hongiksj", 9), "아홀로틀"),
        new QaTransfer(RosterKey.of("hongiksj", 12), "아홀로틀"),
        new QaTransfer(RosterKey.of("soongsil", 31), "수달")
    );

    private QaTransfers() {
    }

    public record QaTransfer(RosterKey member, String toChapter) {
    }
}
