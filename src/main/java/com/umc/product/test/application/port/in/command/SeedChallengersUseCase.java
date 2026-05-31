package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.SeedChallengersCommand;
import com.umc.product.test.application.port.in.command.dto.SeedChallengersResult;

/**
 * 특정 기수에 대해 (Chapter, School, Part) 셀마다 N 명의 더미 회원 + 챌린저를 시딩한다.
 * ADR-017 참조.
 */
public interface SeedChallengersUseCase {

    SeedChallengersResult seed(SeedChallengersCommand command);
}
