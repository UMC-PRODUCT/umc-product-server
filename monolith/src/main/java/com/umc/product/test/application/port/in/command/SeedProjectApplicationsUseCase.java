package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.SeedProjectApplicationsCommand;
import com.umc.product.test.application.port.in.command.dto.SeedProjectApplicationsResult;

/**
 * 지원서 시나리오 시딩 UseCase.
 * <p>
 * 지정 매칭 차수 + 지부 기준으로 아직 팀에 합류하지 않은 챌린저들이 랜덤 프로젝트에
 * 지원서를 제출하고 합불 결정까지 완료하는 시나리오를 실행한다.
 * SQL 직접 주입이 아닌 도메인 UseCase 시퀀스 호출로 만들어진다.
 */
public interface SeedProjectApplicationsUseCase {

    SeedProjectApplicationsResult seed(SeedProjectApplicationsCommand command);
}
