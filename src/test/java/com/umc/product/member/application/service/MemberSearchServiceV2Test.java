package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.member.application.port.in.query.dto.SearchMemberItemV2Info;
import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.application.port.in.query.dto.SearchMemberV2Result;
import com.umc.product.member.application.port.out.SearchMemberPort;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberSearchService.searchByV2 — v2 검색")
class MemberSearchServiceV2Test {

    @Mock SearchMemberPort searchMemberPort;
    @Mock GetMemberUseCase getMemberUseCase;
    @Mock GetChallengerRoleUseCase getChallengerRoleUseCase;
    @Mock GetGisuUseCase getGisuUseCase;

    @InjectMocks MemberSearchService memberSearchService;

    private Challenger createChallenger(Long id, Long memberId, ChallengerPart part, Long gisuId) {
        Challenger c = Challenger.builder()
            .memberId(memberId)
            .part(part)
            .gisuId(gisuId)
            .build();
        ReflectionTestUtils.setField(c, "id", id);
        // status는 builder가 ACTIVE로 기본 세팅하므로 별도 ReflectionTestUtils 호출은 불요
        return c;
    }

    private MemberInfo createProfile(Long id, String name) {
        return MemberInfo.builder()
            .id(id)
            .name(name)
            .nickname(name)
            .email("test@test.com")
            .schoolId(1L)
            .schoolName("학교")
            .status(MemberStatus.ACTIVE)
            .build();
    }

    @Test
    void v2_검색은_challengerStatus를_매핑한다() {
        Pageable pageable = PageRequest.of(0, 10);

        Challenger c1 = createChallenger(1L, 10L, ChallengerPart.WEB, 7L);
        Page<Challenger> page = new PageImpl<>(List.of(c1), pageable, 1);

        given(searchMemberPort.search(any(), any())).willReturn(page);
        given(getMemberUseCase.findAllByIds(anySet())).willReturn(Map.of(10L, createProfile(10L, "홍길동")));
        given(getChallengerRoleUseCase.getAllRoleTypesByChallengerIds(anySet())).willReturn(Map.of());
        given(getGisuUseCase.getByIds(anySet())).willReturn(List.of(
            new GisuInfo(7L, 8L, Instant.now(), Instant.now().plusSeconds(100), true)
        ));
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.empty());

        SearchMemberV2Result result = memberSearchService.searchByV2(
            new SearchMemberQuery(null, null, null, null, null), pageable
        );

        List<SearchMemberItemV2Info> content = result.page().getContent();
        assertThat(content).hasSize(1);
        assertThat(content.get(0).challengerStatus()).isEqualTo(ChallengerStatus.ACTIVE);
        // 활성 기수 정보가 없으므로(휴지기) isAdminInActiveGisu는 항상 false
        assertThat(content.get(0).isAdminInActiveGisu()).isFalse();
    }

    @Test
    void 활성기수_챌린저가_운영진이면_isAdminInActiveGisu가_true다() {
        Pageable pageable = PageRequest.of(0, 10);

        Long activeGisuId = 7L;
        Challenger activeRow = createChallenger(1L, 10L, ChallengerPart.SPRINGBOOT, activeGisuId);
        Page<Challenger> page = new PageImpl<>(List.of(activeRow), pageable, 1);

        given(searchMemberPort.search(any(), any())).willReturn(page);
        given(getMemberUseCase.findAllByIds(anySet())).willReturn(Map.of(10L, createProfile(10L, "운영진")));
        // 검색결과 전체 운영진 매핑 (toItemInfoV2가 한 번, collectAdmin이 한 번 더 호출됨)
        given(getChallengerRoleUseCase.getAllRoleTypesByChallengerIds(anySet())).willReturn(Map.of(
            1L, List.of(ChallengerRoleType.SCHOOL_PRESIDENT)
        ));
        given(getGisuUseCase.getByIds(anySet())).willReturn(List.of(
            new GisuInfo(activeGisuId, 8L, Instant.now(), Instant.now().plusSeconds(100), true)
        ));
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.of(
            new GisuInfo(activeGisuId, 8L, Instant.now(), Instant.now().plusSeconds(100), true)
        ));

        SearchMemberV2Result result = memberSearchService.searchByV2(
            new SearchMemberQuery(null, null, null, null, null), pageable
        );

        SearchMemberItemV2Info item = result.page().getContent().get(0);
        assertThat(item.isAdminInActiveGisu()).isTrue();
        assertThat(item.roleTypes()).containsExactly(ChallengerRoleType.SCHOOL_PRESIDENT);
    }

    @Test
    void 다른_기수_챌린저_행이지만_같은_회원이_활성기수에_운영진이면_isAdminInActiveGisu_true() {
        Pageable pageable = PageRequest.of(0, 10);

        Long activeGisuId = 7L;
        Challenger pastRow = createChallenger(1L, 10L, ChallengerPart.WEB, 6L); // 과거 기수 행
        Challenger activeRow = createChallenger(2L, 10L, ChallengerPart.SPRINGBOOT, activeGisuId);
        Page<Challenger> page = new PageImpl<>(List.of(pastRow, activeRow), pageable, 2);

        given(searchMemberPort.search(any(), any())).willReturn(page);
        given(getMemberUseCase.findAllByIds(anySet())).willReturn(Map.of(10L, createProfile(10L, "운영진")));
        given(getChallengerRoleUseCase.getAllRoleTypesByChallengerIds(anySet())).willReturn(Map.of(
            2L, List.of(ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER)
        ));
        given(getGisuUseCase.getByIds(anySet())).willReturn(List.of(
            new GisuInfo(6L, 7L, Instant.now().minusSeconds(1000), Instant.now().minusSeconds(500), false),
            new GisuInfo(activeGisuId, 8L, Instant.now(), Instant.now().plusSeconds(100), true)
        ));
        given(getGisuUseCase.findActiveGisu()).willReturn(Optional.of(
            new GisuInfo(activeGisuId, 8L, Instant.now(), Instant.now().plusSeconds(100), true)
        ));

        SearchMemberV2Result result = memberSearchService.searchByV2(
            new SearchMemberQuery(null, null, null, null, null), pageable
        );

        List<SearchMemberItemV2Info> content = result.page().getContent();
        // 두 행 모두 isAdminInActiveGisu=true (회원 단위로 확장)
        assertThat(content).extracting(SearchMemberItemV2Info::isAdminInActiveGisu)
            .containsExactly(true, true);
    }
}
