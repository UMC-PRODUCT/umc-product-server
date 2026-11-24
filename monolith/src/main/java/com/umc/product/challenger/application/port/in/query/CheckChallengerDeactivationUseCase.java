package com.umc.product.challenger.application.port.in.query;

public interface CheckChallengerDeactivationUseCase {

    boolean hasRecordsForMember(Long memberId);
}
