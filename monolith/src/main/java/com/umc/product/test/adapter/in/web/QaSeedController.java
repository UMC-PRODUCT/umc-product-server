package com.umc.product.test.adapter.in.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.test.adapter.in.web.dto.QaSeedResponse;
import com.umc.product.test.application.port.in.command.SeedQaDataUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/test/seed/qa")
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Public
@Tag(name = "Test | 시딩")
public class QaSeedController {

    private final SeedQaDataUseCase seedQaDataUseCase;

    @PostMapping
    @Operation(summary = "고정 QA 계정과 업무 데이터 생성", description = """
        요청값 없이 고정 계정 35개와 학습·스터디·출석·공지·상벌점을 생성합니다.
        비밀번호는 기존 app.seed.default-password 설정을 사용합니다.
        대상 이메일이 하나라도 존재하면 중단하며, 생성 중 실패하면 전체 롤백합니다.
        프로젝트·리크루팅은 생성하지 않습니다.
        """)
    public QaSeedResponse seed() {
        return QaSeedResponse.from(seedQaDataUseCase.seed());
    }
}
