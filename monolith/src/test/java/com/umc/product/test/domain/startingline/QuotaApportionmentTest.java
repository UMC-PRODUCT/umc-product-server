package com.umc.product.test.domain.startingline;

import static com.umc.product.common.domain.enums.ChallengerPart.DESIGN;
import static com.umc.product.common.domain.enums.ChallengerPart.MOBILE_PRODUCT_ENGINEER;
import static com.umc.product.common.domain.enums.ChallengerPart.WEB_PRODUCT_ENGINEER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.domain.roster.QaCastFixtures;
import com.umc.product.test.domain.roster.RehearsalCast;
import com.umc.product.test.domain.roster.RosterKey;

@DisplayName("출발선 TO 배분")
class QuotaApportionmentTest {

    private final RehearsalCast cast = QaCastFixtures.cast();

    @Test
    @DisplayName("지부마다 파트별 TO 합계는 매칭 인원의 n/(n+4)를 내림한 값이고 프로젝트마다 개발 파트는 하나다")
    void totalsLeaveRoomForQaProjectsAndEveryProjectHasOneEngineerPart() {
        for (String chapter : cast.chapters()) {
            StartingLinePlan plan = cast.startingLine(chapter);
            Map<ChallengerPart, Integer> headcount = cast.matchingHeadcount(chapter);
            int seeded = plan.seededOwners().size();

            Map<RosterKey, ProjectQuota> quotas = QuotaApportionment.apportion(headcount, plan.seededOwners(), 4);

            assertThat(quotas.keySet()).containsExactlyElementsOf(plan.seededOwners());
            Map<ChallengerPart, Integer> totals = totals(quotas);
            for (ChallengerPart part : RehearsalCast.MATCHING_PARTS) {
                assertThat(totals.getOrDefault(part, 0)).as(chapter + " " + part)
                    .isEqualTo(headcount.get(part) * seeded / (seeded + 4));
            }
            assertThat(quotas.values()).allSatisfy(quota -> {
                assertThat(quota.engineer()).isPositive();
                assertThat(quota.entries().keySet()).doesNotContain(ChallengerPart.PLAN);
            });
        }
    }

    @Test
    @DisplayName("캥거루는 시딩 9개에 디자인 8, Web 37, Mobile 13을 나누고 QA 몫으로 4, 17, 6을 남긴다")
    void kangarooTotals() {
        StartingLinePlan plan = cast.startingLine("캥거루");

        Map<ChallengerPart, Integer> totals = totals(
            QuotaApportionment.apportion(cast.matchingHeadcount("캥거루"), plan.seededOwners(), 4));

        assertThat(cast.matchingHeadcount("캥거루"))
            .containsExactlyInAnyOrderEntriesOf(
                Map.of(DESIGN, 12, WEB_PRODUCT_ENGINEER, 54, MOBILE_PRODUCT_ENGINEER, 19));
        assertThat(totals).containsExactlyInAnyOrderEntriesOf(
            Map.of(DESIGN, 8, WEB_PRODUCT_ENGINEER, 37, MOBILE_PRODUCT_ENGINEER, 13));
    }

    @Test
    @DisplayName("같은 입력이면 같은 배분이 나온다")
    void deterministic() {
        StartingLinePlan plan = cast.startingLine("쿼카");
        Map<ChallengerPart, Integer> headcount = cast.matchingHeadcount("쿼카");

        assertThat(QuotaApportionment.apportion(headcount, plan.seededOwners(), 4))
            .isEqualTo(QuotaApportionment.apportion(headcount, plan.seededOwners(), 4));
    }

    @Test
    @DisplayName("한 개발 파트 인원이 0이면 모든 프로젝트가 다른 개발 파트를 쓴다")
    void skewedHeadcountUsesOnlyAvailableEngineerPart() {
        List<RosterKey> owners = List.of(RosterKey.of("hongiksj", 1), RosterKey.of("hongiksj", 2));

        Map<RosterKey, ProjectQuota> quotas = QuotaApportionment.apportion(
            Map.of(DESIGN, 12, WEB_PRODUCT_ENGINEER, 6, MOBILE_PRODUCT_ENGINEER, 0), owners, 4);

        assertThat(quotas.values()).extracting(ProjectQuota::engineerPart).containsOnly(WEB_PRODUCT_ENGINEER);
        assertThat(totals(quotas)).containsEntry(DESIGN, 4).containsEntry(WEB_PRODUCT_ENGINEER, 2);
    }

    private static Map<ChallengerPart, Integer> totals(Map<RosterKey, ProjectQuota> quotas) {
        Map<ChallengerPart, Integer> totals = new EnumMap<>(ChallengerPart.class);
        quotas.values().forEach(quota -> quota.entries()
            .forEach((part, value) -> totals.merge(part, value, Integer::sum)));
        return totals;
    }
}
