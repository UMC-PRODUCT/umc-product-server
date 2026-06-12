package com.umc.product.organization.application.port.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationQuery;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import com.umc.product.organization.domain.enums.SchoolLinkType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GisuOrganizationQueryService")
class GisuOrganizationQueryServiceTest {

    @Mock
    GetGisuUseCase getGisuUseCase;

    @Mock
    GetChapterUseCase getChapterUseCase;

    @Mock
    GetSchoolUseCase getSchoolUseCase;

    @InjectMocks
    GisuOrganizationQueryService service;

    @Test
    @DisplayName("id 조회는 기수만 반환하고 include가 false면 지부와 학교를 조회하지 않는다")
    void id_조회는_기수만_반환하고_include가_false면_지부와_학교를_조회하지_않는다() {
        GisuInfo gisu9 = gisu(1L, 9L, false);
        GisuInfo gisu10 = gisu(2L, 10L, true);
        given(getGisuUseCase.batchGetByIds(List.of(1L, 2L))).willReturn(List.of(gisu9, gisu10));

        List<GisuOrganizationInfo> result = service.get(GisuOrganizationQuery.byIds(List.of(1L, 2L), false, false));

        assertThat(result).extracting(GisuOrganizationInfo::gisuId).containsExactly(1L, 2L);
        assertThat(result.getFirst().chapters()).isEmpty();
        assertThat(result.getFirst().schools()).isEmpty();
        then(getChapterUseCase).shouldHaveNoInteractions();
        then(getSchoolUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("generation 조회는 지부와 학교를 모두 포함해서 조립한다")
    void generation_조회는_지부와_학교를_모두_포함해서_조립한다() {
        given(getGisuUseCase.batchGetByGenerations(List.of(10L))).willReturn(List.of(gisu(2L, 10L, true)));
        given(getChapterUseCase.getChaptersWithSchoolsByGisuId(2L)).willReturn(List.of(
            new ChapterWithSchoolsInfo(
                11L,
                "서울지부",
                List.of(new ChapterWithSchoolsInfo.SchoolInfo(101L, "중앙대학교"))
            )
        ));
        given(getSchoolUseCase.getSchoolListByGisuId(2L)).willReturn(List.of(
            new SchoolDetailInfo(
                11L,
                "서울지부",
                "중앙대학교",
                101L,
                "비고",
                "https://cdn.example.com/logo.png",
                List.of(new SchoolDetailInfo.SchoolLinkItem("인스타그램", SchoolLinkType.INSTAGRAM, "https://instagram.com/umc")),
                true,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
            )
        ));

        List<GisuOrganizationInfo> result = service.get(
            GisuOrganizationQuery.byGenerations(List.of(10L), true, true)
        );

        GisuOrganizationInfo info = result.getFirst();
        assertThat(info.gisuId()).isEqualTo(2L);
        assertThat(info.chapters()).hasSize(1);
        assertThat(info.chapters().getFirst().schools()).extracting(GisuOrganizationInfo.ChapterSchoolInfo::schoolId)
            .containsExactly(101L);
        assertThat(info.schools()).hasSize(1);
        assertThat(info.schools().getFirst().links()).hasSize(1);
        then(getChapterUseCase).should().getChaptersWithSchoolsByGisuId(2L);
        then(getSchoolUseCase).should().getSchoolListByGisuId(2L);
    }

    @Test
    @DisplayName("지부만 포함하면 지부 내 학교 목록은 비워서 반환한다")
    void 지부만_포함하면_지부_내_학교_목록은_비워서_반환한다() {
        given(getGisuUseCase.batchGetByIds(List.of(2L))).willReturn(List.of(gisu(2L, 10L, true)));
        given(getChapterUseCase.listByGisuId(2L)).willReturn(List.of(new ChapterInfo(11L, "서울지부")));

        List<GisuOrganizationInfo> result = service.get(GisuOrganizationQuery.byIds(List.of(2L), true, false));

        assertThat(result.getFirst().chapters()).hasSize(1);
        assertThat(result.getFirst().chapters().getFirst().schools()).isEmpty();
        assertThat(result.getFirst().schools()).isEmpty();
        then(getChapterUseCase).should().listByGisuId(2L);
        then(getChapterUseCase).should(never()).getChaptersWithSchoolsByGisuId(2L);
        then(getSchoolUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("active 조회는 활성 기수 하나를 반환한다")
    void active_조회는_활성_기수_하나를_반환한다() {
        given(getGisuUseCase.getActiveGisu()).willReturn(gisu(2L, 10L, true));

        List<GisuOrganizationInfo> result = service.get(GisuOrganizationQuery.active(false, false));

        assertThat(result).extracting(GisuOrganizationInfo::gisuId).containsExactly(2L);
    }

    private GisuInfo gisu(Long gisuId, Long generation, boolean active) {
        return new GisuInfo(
            gisuId,
            generation,
            Instant.parse("2026-03-01T00:00:00Z"),
            Instant.parse("2026-08-31T23:59:59Z"),
            active
        );
    }
}
