package com.umc.product.test.adapter.in.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.test.adapter.in.web.dto.RoundSummaryResponse;
import com.umc.product.test.application.port.in.query.GetRoundSummaryUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test/seed/matching/rounds/{roundId}")
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Public
@Tag(name = "Test | 매칭 리허설")
public class MatchingBotController {

    private final GetRoundSummaryUseCase getRoundSummaryUseCase;

    @GetMapping("/summary")
    @Operation(operationId = "SEED-010-S", summary = "매칭 차수 요약", description = """
        차수 하나의 지금 상태를 서버가 DB에서 직접 세어 돌려줍니다. 운영 화면(TO 관리, 매칭 현황, PM 지원자 목록) 숫자가
        맞는지 판단하는 기준값입니다.
        차수(시각, 지금 단계, 자동 선발·발표 여부), 프로젝트·파트별 TO·기존 팀원·지원(봇·QA)·합격·불합격·미결정·채움률,
        파트별 지부 매칭 대상 인원, QA 몫 A·B·C 계정 상태, TO를 못 채운 프로젝트, 지원자 0명 프로젝트를 담습니다.
        지원 수는 제출한 지원서만 셉니다. 봇 지원은 [봇]으로 시작하는 답변이 있는 지원서입니다.
        """)
    public RoundSummaryResponse summary(@PathVariable Long roundId) {
        return RoundSummaryResponse.from(getRoundSummaryUseCase.get(roundId));
    }
}
