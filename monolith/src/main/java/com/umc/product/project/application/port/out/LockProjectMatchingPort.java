package com.umc.product.project.application.port.out;

/** 수동 배정·자동 선발·지부 이동을 같은 기수 안에서 직렬화한다. */
public interface LockProjectMatchingPort {
    void lockByProjectId(Long projectId);

    void lockByRoundId(Long roundId);

    void lockByGisuId(Long gisuId);

    void lockByTransferId(Long transferId);
}
