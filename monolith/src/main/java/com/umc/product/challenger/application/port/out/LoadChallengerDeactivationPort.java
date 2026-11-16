package com.umc.product.challenger.application.port.out;

public interface LoadChallengerDeactivationPort {

    boolean existsByChallengerId(Long challengerId);
}
