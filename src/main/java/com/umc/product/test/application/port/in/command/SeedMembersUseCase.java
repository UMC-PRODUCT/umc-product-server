package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.SeedMembersCommand;
import com.umc.product.test.application.port.in.command.dto.SeedMembersResult;

/**
 * 더미 회원을 N 명 즉시 생성한다. ADR-017 참조.
 */
public interface SeedMembersUseCase {

    SeedMembersResult seed(SeedMembersCommand command);
}
