package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;

import com.umc.product.authorization.application.port.in.query.GetMemberRolesUseCase;
import com.umc.product.challenger.application.port.in.query.dto.GlobalSearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerCursorResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerResult;
import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerItemInfo;
import com.umc.product.challenger.application.port.out.SearchChallengerPort;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
class ChallengerSearchServiceTest {

    @Mock
    SearchChallengerPort searchChallengerPort;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    GetMemberRolesUseCase getMemberRolesUseCase;

    @Mock
    GetGisuUseCase getGisuUseCase;

    @InjectMocks
    ChallengerSearchService challengerSearchService;

    private SearchChallengerQuery defaultQuery;

    private List<Challenger> sixChallengers;
    private Map<Long, MemberProfileInfo> sixProfiles;
    private List<GisuInfo> defaultGisuInfos;

    @BeforeEach
    void setUp() {
        defaultQuery = new SearchChallengerQuery(null, null, null, null, null, null, 1L, List.of(ChallengerStatus.ACTIVE));
        defaultGisuInfos = List.of(
                new GisuInfo(1L, 7L, Instant.now(), Instant.now(), true),
                new GisuInfo(2L, 8L, Instant.now(), Instant.now(), false)
        );

        sixChallengers = List.of(
                createChallenger(1L, 10L, ChallengerPart.PLAN, 1L),
                createChallenger(2L, 11L, ChallengerPart.DESIGN, 1L),
                createChallenger(3L, 12L, ChallengerPart.WEB, 1L),
                createChallenger(4L, 13L, ChallengerPart.IOS, 2L),
                createChallenger(5L, 14L, ChallengerPart.ANDROID, 2L),
                createChallenger(6L, 15L, ChallengerPart.SPRINGBOOT, 2L)
        );

        sixProfiles = Map.of(
                10L, createProfile(10L, "홍길동", "hong"),
                11L, createProfile(11L, "김철수", "kim"),
                12L, createProfile(12L, "이영희", "lee"),
                13L, createProfile(13L, "박민수", "park"),
                14L, createProfile(14L, "최지은", "choi"),
                15L, createProfile(15L, "정하나", "jung")
        );
    }

    @Nested
    @DisplayName("cursorSearch")
    class CursorSearchTest {

        @Test
        void 첫_페이지_조회_시_커서가_null이면_처음부터_조회한다() {
            // given
            int size = 4;
            List<Challenger> firstPage = sixChallengers.subList(0, 4);

            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(firstPage);
            given(searchChallengerPort.countByPart(any())).willReturn(Map.of(
                    ChallengerPart.PLAN, 1L, ChallengerPart.DESIGN, 1L,
                    ChallengerPart.WEB, 1L, ChallengerPart.IOS, 1L
            ));
            given(searchChallengerPort.sumPointsByChallengerIds(anySet())).willReturn(Map.of());
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getMemberRolesUseCase.getRoleTypesByChallengerIds(anySet())).willReturn(Map.of());
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            SearchChallengerCursorResult result = challengerSearchService.cursorSearch(defaultQuery, null, size);

            // then
            assertThat(result.content()).hasSize(4);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.content().get(0).challengerId()).isEqualTo(1L);
            assertThat(result.content().get(3).challengerId()).isEqualTo(4L);
        }

        @Test
        void 다음_페이지가_있으면_hasNext가_true이고_nextCursor는_마지막_항목_ID이다() {
            // given
            int size = 4;
            // 5개 반환 → size+1이므로 hasNext=true, 실제 응답은 4개
            List<Challenger> withExtra = sixChallengers.subList(0, 5);

            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(withExtra);
            given(searchChallengerPort.countByPart(any())).willReturn(Map.of());
            given(searchChallengerPort.sumPointsByChallengerIds(anySet())).willReturn(Map.of());
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getMemberRolesUseCase.getRoleTypesByChallengerIds(anySet())).willReturn(Map.of());
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            SearchChallengerCursorResult result = challengerSearchService.cursorSearch(defaultQuery, null, size);

            // then
            assertThat(result.content()).hasSize(4);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(4L); // 4번째 항목(id=4)이 마지막
            // 5번째 항목(id=5)은 응답에 포함되지 않음
            assertThat(result.content()).noneMatch(item -> item.challengerId().equals(5L));
        }

        @Test
        void 조회_결과가_없으면_빈_리스트와_hasNext_false를_반환한다() {
            // given
            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(List.of());
            given(searchChallengerPort.countByPart(any())).willReturn(Map.of());

            // when
            SearchChallengerCursorResult result = challengerSearchService.cursorSearch(defaultQuery, null, 4);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
        }

        @Test
        void 챌린저별_역할_정보가_각각_올바르게_매핑된다() {
            // given
            int size = 6;
            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(sixChallengers);
            given(searchChallengerPort.countByPart(any())).willReturn(Map.of());
            given(searchChallengerPort.sumPointsByChallengerIds(anySet())).willReturn(Map.of());
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getMemberRolesUseCase.getRoleTypesByChallengerIds(anySet())).willReturn(Map.of(
                    1L, List.of(ChallengerRoleType.CENTRAL_PRESIDENT),
                    3L, List.of(ChallengerRoleType.SCHOOL_PRESIDENT, ChallengerRoleType.SCHOOL_PART_LEADER),
                    5L, List.of(ChallengerRoleType.CHAPTER_PRESIDENT)
                    // 2, 4, 6번은 역할 없음
            ));
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            SearchChallengerCursorResult result = challengerSearchService.cursorSearch(defaultQuery, null, size);

            // then
            assertThat(result.content()).hasSize(6);
            // 역할이 있는 챌린저
            assertThat(result.content().get(0).roleTypes())
                    .containsExactly(ChallengerRoleType.CENTRAL_PRESIDENT);
            assertThat(result.content().get(2).roleTypes())
                    .containsExactlyInAnyOrder(ChallengerRoleType.SCHOOL_PRESIDENT, ChallengerRoleType.SCHOOL_PART_LEADER);
            assertThat(result.content().get(4).roleTypes())
                    .containsExactly(ChallengerRoleType.CHAPTER_PRESIDENT);
            // 역할이 없는 챌린저
            assertThat(result.content().get(1).roleTypes()).isEmpty();
            assertThat(result.content().get(3).roleTypes()).isEmpty();
            assertThat(result.content().get(5).roleTypes()).isEmpty();
        }

        @Test
        void 포인트_합계가_챌린저별로_정확히_반영된다() {
            // given
            int size = 6;
            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(sixChallengers);
            given(searchChallengerPort.countByPart(any())).willReturn(Map.of());
            given(searchChallengerPort.sumPointsByChallengerIds(anySet())).willReturn(Map.of(
                    1L, 0.5,   // WARNING 1회
                    3L, 1.5,   // WARNING 1회 + OUT 1회
                    4L, 2.0    // OUT 2회
                    // 2, 5, 6번은 상벌점 없음
            ));
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getMemberRolesUseCase.getRoleTypesByChallengerIds(anySet())).willReturn(Map.of());
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            SearchChallengerCursorResult result = challengerSearchService.cursorSearch(defaultQuery, null, size);

            // then
            assertThat(result.content().get(0).pointSum()).isEqualTo(0.5);
            assertThat(result.content().get(1).pointSum()).isEqualTo(0.0); // 기본값
            assertThat(result.content().get(2).pointSum()).isEqualTo(1.5);
            assertThat(result.content().get(3).pointSum()).isEqualTo(2.0);
            assertThat(result.content().get(4).pointSum()).isEqualTo(0.0);
            assertThat(result.content().get(5).pointSum()).isEqualTo(0.0);
        }

        @Test
        void 파트별_카운트가_모든_파트에_대해_응답에_포함된다() {
            // given
            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(List.of());
            given(searchChallengerPort.countByPart(any())).willReturn(Map.of(
                    ChallengerPart.PLAN, 2L,
                    ChallengerPart.WEB, 5L,
                    ChallengerPart.IOS, 3L,
                    ChallengerPart.SPRINGBOOT, 4L
            ));

            // when
            SearchChallengerCursorResult result = challengerSearchService.cursorSearch(defaultQuery, null, 4);

            // then
            assertThat(result.partCounts()).containsEntry(ChallengerPart.PLAN, 2L);
            assertThat(result.partCounts()).containsEntry(ChallengerPart.WEB, 5L);
            assertThat(result.partCounts()).containsEntry(ChallengerPart.IOS, 3L);
            assertThat(result.partCounts()).containsEntry(ChallengerPart.SPRINGBOOT, 4L);
            // 값이 없는 파트는 0으로 초기화
            assertThat(result.partCounts()).containsEntry(ChallengerPart.DESIGN, 0L);
            assertThat(result.partCounts()).containsEntry(ChallengerPart.ANDROID, 0L);
            assertThat(result.partCounts()).containsEntry(ChallengerPart.NODEJS, 0L);
        }

        @Test
        void 멤버_프로필이_존재하지_않으면_예외가_발생한다() {
            // given
            int size = 6;

            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(sixChallengers);
            given(searchChallengerPort.countByPart(any())).willReturn(Map.of());
            given(searchChallengerPort.sumPointsByChallengerIds(anySet())).willReturn(Map.of());
            given(getMemberUseCase.getProfiles(anySet())).willReturn(Map.of()); // 프로필 없음
            given(getMemberRolesUseCase.getRoleTypesByChallengerIds(anySet())).willReturn(Map.of());
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when & then
            assertThatThrownBy(() -> challengerSearchService.cursorSearch(defaultQuery, null, size))
                    .isInstanceOf(ChallengerDomainException.class);
        }

        @Test
        void 유효하지_않은_커서_ID가_전달되면_예외가_발생한다() {
            // given
            given(searchChallengerPort.cursorSearch(any(), any(), anyInt()))
                    .willThrow(new ChallengerDomainException(
                            com.umc.product.challenger.domain.exception.ChallengerErrorCode.INVALID_CURSOR_ID));

            // when & then
            assertThatThrownBy(() -> challengerSearchService.cursorSearch(defaultQuery, 9999L, 4))
                    .isInstanceOf(ChallengerDomainException.class);
        }

        @Test
        void 마지막_페이지에서_size보다_적은_결과가_반환되면_hasNext는_false이다() {
            // given
            int size = 4;
            // 마지막 페이지: 2명만 남음
            List<Challenger> lastPage = sixChallengers.subList(4, 6);

            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(lastPage);
            given(searchChallengerPort.countByPart(any())).willReturn(Map.of());
            given(searchChallengerPort.sumPointsByChallengerIds(anySet())).willReturn(Map.of());
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getMemberRolesUseCase.getRoleTypesByChallengerIds(anySet())).willReturn(Map.of());
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            SearchChallengerCursorResult result = challengerSearchService.cursorSearch(defaultQuery, 4L, size);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.content().get(0).challengerId()).isEqualTo(5L);
            assertThat(result.content().get(1).challengerId()).isEqualTo(6L);
        }
    }

    @Nested
    @DisplayName("search (offset 기반)")
    class OffsetSearchTest {

        @Test
        void offset_기반_검색에도_역할_정보가_포함된다() {
            // given
            Pageable pageable = PageRequest.of(0, 4);
            List<Challenger> pageContent = sixChallengers.subList(0, 4);
            Page<Challenger> page = new PageImpl<>(pageContent, pageable, 6);

            given(searchChallengerPort.search(any(), any())).willReturn(page);
            given(searchChallengerPort.countByPart(any())).willReturn(Map.of());
            given(searchChallengerPort.sumPointsByChallengerIds(anySet())).willReturn(Map.of());
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getMemberRolesUseCase.getRoleTypesByChallengerIds(anySet())).willReturn(Map.of(
                    1L, List.of(ChallengerRoleType.CENTRAL_PRESIDENT),
                    3L, List.of(ChallengerRoleType.SCHOOL_PRESIDENT)
            ));
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            SearchChallengerResult result = challengerSearchService.search(defaultQuery, pageable);

            // then
            assertThat(result.page().getContent()).hasSize(4);
            assertThat(result.page().getContent().get(0).roleTypes())
                    .containsExactly(ChallengerRoleType.CENTRAL_PRESIDENT);
            assertThat(result.page().getContent().get(1).roleTypes()).isEmpty();
            assertThat(result.page().getContent().get(2).roleTypes())
                    .containsExactly(ChallengerRoleType.SCHOOL_PRESIDENT);
            assertThat(result.page().getContent().get(3).roleTypes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("globalCursorSearch")
    class GlobalCursorSearchTest {

        private SearchChallengerQuery globalQuery;

        @BeforeEach
        void setUp() {
            globalQuery = new SearchChallengerQuery(null, "홍", null, null, null, null, null, List.of(ChallengerStatus.ACTIVE, ChallengerStatus.GRADUATED));
        }

        @Test
        void 첫_페이지_조회_시_커서가_null이면_처음부터_조회한다() {
            // given
            int size = 4;
            List<Challenger> firstPage = sixChallengers.subList(0, 4);

            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(firstPage);
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            GlobalSearchChallengerCursorResult result = challengerSearchService.globalCursorSearch(globalQuery, null, size);

            // then
            assertThat(result.content()).hasSize(4);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
        }

        @Test
        void 다음_페이지가_있으면_hasNext가_true이고_nextCursor는_마지막_항목_ID이다() {
            // given
            int size = 4;
            List<Challenger> withExtra = sixChallengers.subList(0, 5);

            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(withExtra);
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            GlobalSearchChallengerCursorResult result = challengerSearchService.globalCursorSearch(globalQuery, null, size);

            // then
            assertThat(result.content()).hasSize(4);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isEqualTo(4L);
            assertThat(result.content()).noneMatch(item -> item.memberId().equals(14L));
        }

        @Test
        void 조회_결과가_없으면_빈_리스트와_hasNext_false를_반환한다() {
            // given
            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(List.of());

            // when
            GlobalSearchChallengerCursorResult result = challengerSearchService.globalCursorSearch(globalQuery, null, 4);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
        }

        @Test
        void 응답에_닉네임_이름_학교명_기수_프로필이미지가_포함된다() {
            // given
            int size = 6;
            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(sixChallengers);
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            GlobalSearchChallengerCursorResult result = challengerSearchService.globalCursorSearch(globalQuery, null, size);

            // then
            assertThat(result.content()).hasSize(6);

            var first = result.content().get(0);
            assertThat(first.memberId()).isEqualTo(10L);
            assertThat(first.name()).isEqualTo("홍길동");
            assertThat(first.nickname()).isEqualTo("hong");
            assertThat(first.schoolName()).isEqualTo("한양대학교ERICA");
            assertThat(first.gisu()).isEqualTo(7L);
            assertThat(first.part()).isEqualTo(ChallengerPart.PLAN);
            assertThat(first.profileImageLink()).isNull();
        }

        @Test
        void gisuId에_따라_실제_기수_번호가_올바르게_매핑된다() {
            // given
            int size = 6;
            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(sixChallengers);
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            GlobalSearchChallengerCursorResult result = challengerSearchService.globalCursorSearch(globalQuery, null, size);

            // then
            // gisuId=1 → gisu=7
            assertThat(result.content().get(0).gisu()).isEqualTo(7L);
            assertThat(result.content().get(1).gisu()).isEqualTo(7L);
            assertThat(result.content().get(2).gisu()).isEqualTo(7L);
            // gisuId=2 → gisu=8
            assertThat(result.content().get(3).gisu()).isEqualTo(8L);
            assertThat(result.content().get(4).gisu()).isEqualTo(8L);
            assertThat(result.content().get(5).gisu()).isEqualTo(8L);
        }

        @Test
        void 멤버_프로필이_존재하지_않으면_예외가_발생한다() {
            // given
            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(sixChallengers);
            given(getMemberUseCase.getProfiles(anySet())).willReturn(Map.of());
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when & then
            assertThatThrownBy(() -> challengerSearchService.globalCursorSearch(globalQuery, null, 6))
                    .isInstanceOf(ChallengerDomainException.class);
        }

        @Test
        void 마지막_페이지에서_size보다_적은_결과가_반환되면_hasNext는_false이다() {
            // given
            int size = 4;
            List<Challenger> lastPage = sixChallengers.subList(4, 6);

            given(searchChallengerPort.cursorSearch(any(), any(), anyInt())).willReturn(lastPage);
            given(getMemberUseCase.getProfiles(anySet())).willReturn(sixProfiles);
            given(getGisuUseCase.getByIds(anySet())).willReturn(defaultGisuInfos);

            // when
            GlobalSearchChallengerCursorResult result = challengerSearchService.globalCursorSearch(globalQuery, 4L, size);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.content().get(0).memberId()).isEqualTo(14L);
            assertThat(result.content().get(1).memberId()).isEqualTo(15L);
        }
    }

    private Challenger createChallenger(Long id, Long memberId, ChallengerPart part, Long gisuId) {
        Challenger challenger = Challenger.builder()
                .memberId(memberId)
                .part(part)
                .gisuId(gisuId)
                .build();
        ReflectionTestUtils.setField(challenger, "id", id);
        return challenger;
    }

    private MemberProfileInfo createProfile(Long id, String name, String nickname) {
        return new MemberProfileInfo(
                id, name, nickname, name + "@test.com",
                1L, "한양대학교ERICA", null, MemberStatus.ACTIVE, null
        );
    }
}
