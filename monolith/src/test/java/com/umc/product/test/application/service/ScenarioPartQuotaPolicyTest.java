package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.UpdatePartQuotasCommand.Entry;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScenarioPartQuotaPolicyTest {

    private static final Set<ChallengerPart> FRONTEND_PARTS = Set.of(
        ChallengerPart.WEB, ChallengerPart.ANDROID, ChallengerPart.IOS
    );
    private static final Set<ChallengerPart> BACKEND_PARTS = Set.of(
        ChallengerPart.NODEJS, ChallengerPart.SPRINGBOOT
    );

    private final ScenarioPartQuotaPolicy sut = new ScenarioPartQuotaPolicy();

    @Test
    @DisplayName("pickQuotas는 DESIGN 1, FE 1, BE 1로 항상 3개 entry를 반환한다")
    void pickQuotas_returns_three_entries_with_design_fe_be() {
        for (int i = 0; i < 200; i++) {
            List<Entry> quotas = sut.pickQuotas();

            assertThat(quotas).hasSize(3);
            assertThat(quotas.get(0).part()).isEqualTo(ChallengerPart.DESIGN);
            assertThat(FRONTEND_PARTS).contains(quotas.get(1).part());
            assertThat(BACKEND_PARTS).contains(quotas.get(2).part());
        }
    }

    @Test
    @DisplayName("DESIGN quota는 항상 1~2 사이")
    void design_quota_is_within_one_to_two() {
        for (int i = 0; i < 200; i++) {
            List<Entry> quotas = sut.pickQuotas();
            assertThat(quotas.get(0).quota()).isBetween(1L, 2L);
        }
    }

    @Test
    @DisplayName("FE quota와 BE quota는 항상 3~4 사이")
    void fe_and_be_quota_are_within_three_to_four() {
        for (int i = 0; i < 200; i++) {
            List<Entry> quotas = sut.pickQuotas();
            assertThat(quotas.get(1).quota()).isBetween(3L, 4L);
            assertThat(quotas.get(2).quota()).isBetween(3L, 4L);
        }
    }
}
