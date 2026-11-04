package com.umc.product.test.adapter.in.web;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.test.adapter.in.web.dto.QaSeedResponse;
import com.umc.product.test.application.port.in.command.SeedQaDataUseCase;
import com.umc.product.test.application.port.in.command.dto.QaSeedStep;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test/seed/qa")
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Public
@Tag(name = "Test | 시딩")
public class QaSeedController {

    private final SeedQaDataUseCase seedQaDataUseCase;

    @PostMapping
    @Operation(summary = "QA 기본 데이터 전체 생성", description = """
        계정 → 학습 → 출석 → 공지 → 상벌점 → 채팅 순서로 단계를 실행하고 단계별 결과를 돌려줍니다.
        계정은 11기 명단 583명, superadmin, 10기 기존 QA 계정 9개로 모두 593개입니다.
        단계마다 따로 저장하므로 한 단계가 실패해도 앞 단계는 남습니다. 계정 단계가 실패하면 뒤 단계는 NOT_RUN입니다.
        다시 부르면 이미 있는 것은 건너뛰고 UNCHANGED를 돌려줍니다. 되돌리려면 DB를 초기화합니다.
        비밀번호는 app.seed.default-password, 이메일 도메인은 app.seed.email-domain 설정을 씁니다.
        """)
    public QaSeedResponse seedAll() {
        return QaSeedResponse.from(seedQaDataUseCase.seedAll());
    }

    @PostMapping("/{step}")
    @Operation(summary = "QA 기본 데이터 한 단계 생성", description = """
        한 단계만 실행합니다. 이미 있는 것은 건너뜁니다. 계정 단계를 먼저 실행해야 나머지 단계가 계정을 찾습니다.
        """)
    public QaSeedResponse seed(
        @Parameter(description = "accounts, learning, attendance, notices, points, chat")
        @PathVariable String step
    ) {
        return QaSeedResponse.from(List.of(seedQaDataUseCase.seed(QaSeedStep.fromPath(step))));
    }
}
