package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.SeedCommunityCommand;
import com.umc.product.test.application.port.in.command.dto.SeedCommunityResult;

/**
 * 활성 기수의 챌린저 풀을 작성자로 사용해 Post · Comment · Trophy 를 시딩한다. ADR-017 참조.
 */
public interface SeedCommunityUseCase {

    SeedCommunityResult seed(SeedCommunityCommand command);
}
