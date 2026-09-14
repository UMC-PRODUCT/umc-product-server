package com.umc.product.challenger.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.out.dto.ChallengerSearchBundle;
import com.umc.product.challenger.application.port.out.dto.ChallengerSearchRow;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.ChallengerTrack;
import com.umc.product.common.domain.enums.GisuLearningType;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.command.SaveGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("챌린저 검색 트랙 배열 쿼리 통합 테스트")
class ChallengerTrackSearchQueryTest extends IntegrationTestSupport {

    @Autowired
    private ChallengerQueryRepository repository;
    @Autowired
    private SaveGisuPort saveGisuPort;
    @Autowired
    private SaveMemberPort saveMemberPort;
    @Autowired
    private com.umc.product.challenger.application.port.out.SaveChallengerPort saveChallengerPort;

    private Gisu gisu;
    private Long web1;
    private Long webInfra;
    private Long mobile;
    private Long plan;

    @BeforeEach
    void 준비() {
        gisu = saveGisuPort.save(Gisu.create(7311L,
            Instant.parse("2026-09-01T00:00:00Z"), Instant.parse("2027-02-28T00:00:00Z"),
            false, GisuLearningType.TRACK));
        web1 = enroll("웹일", "web1@track.test", List.of(ChallengerTrack.WEB_PRODUCT_ENGINEER));
        webInfra = enroll("웹인프라", "webinfra@track.test",
            List.of(ChallengerTrack.WEB_PRODUCT_ENGINEER, ChallengerTrack.INFRA_PLUS));
        mobile = enroll("앱하나", "mobile@track.test", List.of(ChallengerTrack.MOBILE_PRODUCT_ENGINEER));
        plan = enroll("기획자", "plan@track.test", List.of(ChallengerTrack.PLAN));
    }

    private Long enroll(String name, String email, List<ChallengerTrack> tracks) {
        Member member = saveMemberPort.save(Member.create(name, name, email, 900L, null));
        Challenger challenger = saveChallengerPort.save(Challenger.builder()
            .memberId(member.getId()).gisuId(gisu.getId()).tracks(tracks).build());
        return challenger.getMemberId();
    }

    @Test
    @DisplayName("트랙 배열을 프로젝션으로 함께 조회하고 트랙별 카운트를 집계한다")
    void 트랙_프로젝션과_카운트() {
        ChallengerSearchBundle bundle = repository.pagingSearchWithCounts(
            query(null), PageRequest.of(0, 20));

        assertThat(bundle.rows()).hasSize(4);
        ChallengerSearchRow webInfraRow = bundle.rows().stream()
            .filter(row -> row.memberId().equals(webInfra)).findFirst().orElseThrow();
        assertThat(webInfraRow.tracks()).containsExactly(
            ChallengerTrack.WEB_PRODUCT_ENGINEER, ChallengerTrack.INFRA_PLUS);

        assertThat(bundle.trackCounts())
            .containsEntry(ChallengerTrack.WEB_PRODUCT_ENGINEER, 2L)
            .containsEntry(ChallengerTrack.MOBILE_PRODUCT_ENGINEER, 1L)
            .containsEntry(ChallengerTrack.PLAN, 1L)
            .containsEntry(ChallengerTrack.INFRA_PLUS, 1L)
            .containsEntry(ChallengerTrack.DESIGN, 0L);
    }

    @Test
    @DisplayName("track 필터는 tracks 배열에 해당 트랙이 포함된 챌린저만 반환한다")
    void 트랙_필터() {
        ChallengerSearchBundle bundle = repository.pagingSearchWithCounts(
            query(ChallengerTrack.WEB_PRODUCT_ENGINEER), PageRequest.of(0, 20));

        assertThat(bundle.rows()).extracting(ChallengerSearchRow::memberId)
            .containsExactlyInAnyOrder(web1, webInfra);
    }

    private SearchChallengerQuery query(ChallengerTrack track) {
        return new SearchChallengerQuery(null, null, null, null, null, null, null, track,
            gisu.getId(), List.of(ChallengerStatus.ACTIVE));
    }
}
