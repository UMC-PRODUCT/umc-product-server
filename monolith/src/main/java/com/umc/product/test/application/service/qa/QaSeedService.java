package com.umc.product.test.application.service.qa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.test.application.port.in.command.SeedQaDataUseCase;
import com.umc.product.test.application.port.in.command.dto.QaSeedResult;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class QaSeedService implements SeedQaDataUseCase {
    private final QaBaseSeedService baseSeedService;
    private final QaLearningSeedScenario learningSeedScenario;
    private final QaAttendanceSeedScenario attendanceSeedScenario;
    private final QaNoticePointSeedScenario noticePointSeedScenario;

    @Override
    @Transactional
    public QaSeedResult seed() {
        QaSeedContext context = baseSeedService.seed();
        learningSeedScenario.seed(context);
        attendanceSeedScenario.seed(context);
        noticePointSeedScenario.seed(context);
        return new QaSeedResult(context.members().size());
    }
}
