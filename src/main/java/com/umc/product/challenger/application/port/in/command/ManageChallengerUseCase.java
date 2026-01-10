package com.umc.product.challenger.application.port.in.command;

public interface ManageChallengerUseCase {
    /**
     * 챌린저 상벌점 부여
     */
    void givePointsToChallenger();

    // 역할이라고 함은, 운영진 중 일부를 의미

    /**
     * 챌린저에게 역할을 부여
     */
    void assignRoleToChallenger();

    /**
     * 챌린저 역할 회수
     */
    void revokeRoleFromChallenger();
}
