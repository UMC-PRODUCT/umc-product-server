package com.umc.product.project.adapter.in.scheduler;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.umc.product.project.application.port.in.command.AutoDecideProjectMatchingRoundUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchingRoundDeadlineHandlerTest {

    @Mock
    AutoDecideProjectMatchingRoundUseCase autoDecideUseCase;

    @InjectMocks
    MatchingRoundDeadlineHandler sut;

    @Test
    void handle은_autoDecide를_null_executor로_호출한다() {
        sut.handle(42L);

        then(autoDecideUseCase).should().autoDecide(42L, null);
    }

    @Test
    void autoDecide_예외는_swallow되어_상위로_전파되지_않는다() {
        willThrow(new RuntimeException("boom")).given(autoDecideUseCase).autoDecide(42L, null);

        sut.handle(42L);
        // 예외 미전파 — 호출이 정상 종료
    }
}
