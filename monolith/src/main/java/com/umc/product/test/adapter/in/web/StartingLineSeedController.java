package com.umc.product.test.adapter.in.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.test.adapter.in.web.dto.StartingLineResponse;
import com.umc.product.test.application.port.in.command.SeedStartingLineUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test/seed/projects")
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Public
@Tag(name = "Test | 시딩")
public class StartingLineSeedController {

    private final SeedStartingLineUseCase seedStartingLineUseCase;

    @PostMapping("/starting-line")
    @Operation(operationId = "SEED-003-L", summary = "프로젝트 출발선 생성", description = """
        11기 다섯 지부의 매칭 리허설 출발선을 만듭니다. QA 시딩(/test/seed/qa)을 먼저 실행해야 합니다.
        지부마다 명단 순서상 마지막 PLAN 2명은 프로젝트 없이 둡니다.
        첫 PLAN 2명은 QA 프로젝트로, PO가 정보와 지원 폼을 쓰고 제출해 승인 대기·TO 비움 상태로 둡니다.
        나머지 PLAN은 PO가 제출한 뒤 superadmin이 TO를 넣고 공개합니다.
        개발 파트는 Web PE와 Mobile PE 중 하나만 씁니다. 파트별 TO 합계는 지부 매칭 인원(지부 이동 반영)의
        n/(n+4)이고, 나머지는 QA 프로젝트 2개와 직접 등록 2개에 QA가 TO를 넣을 몫으로 남깁니다.
        다시 부르면 PO마다 지금 상태에서 남은 단계만 실행합니다. 처음부터 다시 하려면 DELETE /test/seed/projects 뒤에 부릅니다.
        """)
    public StartingLineResponse seed() {
        return StartingLineResponse.from(seedStartingLineUseCase.seed());
    }
}
