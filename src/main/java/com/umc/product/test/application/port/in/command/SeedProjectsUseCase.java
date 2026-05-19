package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.SeedProjectsCommand;
import com.umc.product.test.application.port.in.command.dto.SeedProjectsResult;

/**
 * 같은 school 멤버 풀에서 프로젝트와 프로젝트 멤버를 시딩한다. ADR-017 참조.
 */
public interface SeedProjectsUseCase {

    SeedProjectsResult seed(SeedProjectsCommand command);
}
