package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosCommand;
import com.umc.product.test.application.port.in.command.dto.SeedProjectScenariosResult;

/**
 * 프로젝트 시나리오 시딩 UseCase.
 * <p>
 * 활성 기수에 대해 DRAFT / PENDING_REVIEW / IN_PROGRESS 중 하나의 상태까지 도달한 프로젝트를
 * 생성한다. SQL 직접 주입이 아닌 도메인 UseCase 시퀀스 호출로 만들어진다.
 */
public interface SeedProjectScenariosUseCase {

    SeedProjectScenariosResult seed(SeedProjectScenariosCommand command);
}
