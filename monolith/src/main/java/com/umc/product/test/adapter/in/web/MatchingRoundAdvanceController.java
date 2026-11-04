package com.umc.product.test.adapter.in.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.test.adapter.in.web.dto.AdvanceMatchingRoundRequest;
import com.umc.product.test.adapter.in.web.dto.AdvanceMatchingRoundResponse;
import com.umc.product.test.application.port.in.command.AdvanceMatchingRoundUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test/seed/matching/rounds")
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Public
@Tag(name = "Test | 매칭 리허설")
public class MatchingRoundAdvanceController {

    private final AdvanceMatchingRoundUseCase advanceMatchingRoundUseCase;

    @PostMapping("/{roundId}/advance")
    @Operation(operationId = "SEED-010-N", summary = "매칭 차수 한 단계 넘기기", description = """
        차수 하나의 시각을 테스트 전용 SQL로 다음 단계로 당깁니다. 1차가 시작되면 운영진 화면으로는 시각을 바꿀 수 없기 때문입니다.
        1·2·3차는 APPLICATION_START → APPLICATION_CLOSE → DECISION_DEADLINE → RESULT_PUBLISH,
        랜덤 매칭은 ADJUSTMENT_START → ADJUSTMENT_END → FINAL_PUBLISH 순서로 한 칸씩만 넘깁니다.
        넘긴 단계 시각은 지금이 되고, 아직 오지 않은 단계와 같은 지부·유형 뒤 차수의 시각 중 내일 같은 시각보다 이른 것은
        내일 이후 한 시간 간격으로 밉니다. 그보다 늦게 등록한 시각은 그대로 둡니다.
        자동 선발과 발표는 직접 실행하지 않습니다. 시각을 고친 뒤 알람을 다시 맞추면 실제 스케줄러가
        선발 마감 1분 뒤 자동 선발을, 발표 시각에 결과 공개를 실행합니다. 발표로 넘길 때는 선발 마감을 5분 이상 앞으로 당깁니다.
        이미 지난 단계를 다시 보내면 시각은 그대로 두고 알람만 다시 맞춥니다(ALREADY_REACHED).
        거부: 앞 차수 미발표 SEED-0301, 자동 선발 전 발표 SEED-0302, 순서 건너뛰기 SEED-0303, 랜덤 배정 전 조정 SEED-0304.
        알람은 서버 메모리에만 있어, 재시작하면 DB 시각 기준으로 다시 등록됩니다.
        응답의 summary는 알람을 맞춘 뒤의 차수 요약으로, GET summary와 같은 모양입니다.
        """)
    public AdvanceMatchingRoundResponse advance(
        @PathVariable Long roundId,
        @RequestBody @Valid AdvanceMatchingRoundRequest request
    ) {
        return AdvanceMatchingRoundResponse.from(advanceMatchingRoundUseCase.advance(roundId, request.step()));
    }
}
