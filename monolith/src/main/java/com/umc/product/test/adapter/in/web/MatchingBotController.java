package com.umc.product.test.adapter.in.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.test.adapter.in.web.dto.BotApplyRequest;
import com.umc.product.test.adapter.in.web.dto.BotDecisionRequest;
import com.umc.product.test.adapter.in.web.dto.BotRunResponse;
import com.umc.product.test.adapter.in.web.dto.RoundSummaryResponse;
import com.umc.product.test.application.port.in.command.RunMatchingBotUseCase;
import com.umc.product.test.application.port.in.query.GetRoundSummaryUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final RunMatchingBotUseCase runMatchingBotUseCase;

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

    @PostMapping("/bot-applications")
    @Operation(operationId = "SEED-010-A", summary = "봇 지원", description = """
        지원 기간(지원 시작~지원 마감)에만 부를 수 있습니다. 아니면 409 SEED-0305.
        대상은 이 차수 지부를 매칭 지부로 가진, 차수 유형 파트(디자인 또는 Web·Mobile PE)의 팀 없는 사람입니다.
        QA 몫 A·B·C 계정과 excludeMemberIds는 뺍니다. 그중 applyRate(기본 0.8)만큼이 프로젝트 한 곳에 지원합니다.
        그 파트 TO가 남은 공개 프로젝트 중 인기도(PO 계정으로 고정)에 비례해 고르고, 직전 차수에 떨어진 프로젝트는
        고르지 않습니다. QA 프로젝트에도 지원합니다. 주관식 답변은 [봇]으로 시작하고 객관식은 첫 선택지입니다.
        지원자 한 명은 초안·답변·제출을 한 번에 저장합니다. 다시 부르면 이미 지원한 사람은 건너뛰어 지원자가 늘지 않습니다.
        seed 기본값은 (지부, 유형, 차수 번호)라 프로젝트를 초기화해도 같은 사람이 같은 PO의 프로젝트에 지원합니다.
        """)
    public BotRunResponse applyBot(
        @PathVariable Long roundId,
        @RequestBody(required = false) @Valid BotApplyRequest request
    ) {
        return BotRunResponse.from(runMatchingBotUseCase.apply(roundId,
            request == null ? BotApplyRequest.defaults() : request.toParams()));
    }

    @PostMapping("/bot-decisions")
    @Operation(operationId = "SEED-010-D", summary = "봇 PM 결정", description = """
        지원 마감 뒤부터 선발 마감 전(자동 선발 전)까지만 부를 수 있습니다. 아니면 409 SEED-0306.
        지부의 QA 프로젝트 2개와 excludeProjectIds는 결정하지 않습니다.
        프로젝트·파트마다 목표 = max(올림(fillRate × 지터 × TO) − 이미 채운 인원, 최소 선발 인원)이고 남은 TO와 지원자
        수를 넘지 않습니다. fillRate 기본은 1차 0.6, 2차 0.85, 3차 1.0이고 지터는 프로젝트마다 1±jitter(기본 0.2)로
        고정합니다. 목표만큼 합격시키고 남은 지원자의 절반은 불합격, 절반은 미결정으로 둡니다. 결정자는 프로젝트 PO입니다.
        이미 결정된 지원서는 건드리지 않으므로 다시 부르면 바뀌는 것이 없습니다.
        """)
    public BotRunResponse decideBot(
        @PathVariable Long roundId,
        @RequestBody(required = false) @Valid BotDecisionRequest request
    ) {
        return BotRunResponse.from(runMatchingBotUseCase.decide(roundId,
            request == null ? BotDecisionRequest.defaults() : request.toParams()));
    }
}
