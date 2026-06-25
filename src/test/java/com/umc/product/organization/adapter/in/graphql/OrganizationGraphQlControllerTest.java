package com.umc.product.organization.adapter.in.graphql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuOrganizationUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo.ChapterOrganizationInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo.ChapterSchoolInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo.SchoolLinkInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo.SchoolOrganizationInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationQuery;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolNameInfo;
import com.umc.product.organization.domain.enums.SchoolLinkType;

@GraphQlTest(OrganizationGraphQlController.class)
@DisplayName("OrganizationGraphQlController")
class OrganizationGraphQlControllerTest {

    @Autowired
    GraphQlTester graphQlTester;

    @MockitoBean
    GetGisuOrganizationUseCase getGisuOrganizationUseCase;

    @MockitoBean
    GetGisuUseCase getGisuUseCase;

    @MockitoBean
    GetChapterUseCase getChapterUseCase;

    @MockitoBean
    GetSchoolUseCase getSchoolUseCase;

    @Test
    @DisplayName("기수 조직 조회는 id 중복을 제거하고 지부와 학교 포함 옵션을 전달한다")
    void 기수_조직_조회는_id_중복을_제거하고_지부와_학교_포함_옵션을_전달한다() {
        given(getGisuOrganizationUseCase.get(any())).willReturn(List.of(gisuOrganization()));

        graphQlTester.document("""
                query {
                  gisuOrganizations(input: {
                    ids: [1, 1, 2]
                    includeChapters: true
                    includeSchools: true
                  }) {
                    gisus {
                      gisuId
                      generation
                      active
                      startAt
                      chapters {
                        chapterId
                        chapterName
                        schools {
                          schoolId
                          schoolName
                        }
                      }
                      schools {
                        schoolId
                        schoolName
                        active
                        links {
                          title
                          type
                          url
                        }
                      }
                    }
                  }
                }
                """)
            .execute()
            .path("gisuOrganizations.gisus[0].gisuId").entity(String.class).isEqualTo("1")
            .path("gisuOrganizations.gisus[0].generation").entity(String.class).isEqualTo("10")
            .path("gisuOrganizations.gisus[0].active").entity(Boolean.class).isEqualTo(true)
            .path("gisuOrganizations.gisus[0].chapters[0].chapterName").entity(String.class).isEqualTo("Ain 지부")
            .path("gisuOrganizations.gisus[0].schools[0].links[0].type").entity(String.class)
            .isEqualTo("INSTAGRAM");

        ArgumentCaptor<GisuOrganizationQuery> captor = ArgumentCaptor.forClass(GisuOrganizationQuery.class);
        then(getGisuOrganizationUseCase).should().get(captor.capture());
        GisuOrganizationQuery query = captor.getValue();
        assertThat(query.selector()).isEqualTo(GisuOrganizationQuery.Selector.ID);
        assertThat(query.ids()).containsExactly(1L, 2L);
        assertThat(query.generations()).isEmpty();
        assertThat(query.includeChapter()).isTrue();
        assertThat(query.includeSchool()).isTrue();
    }

    @Test
    @DisplayName("기수 조직 조회는 generation 중복을 제거한다")
    void 기수_조직_조회는_generation_중복을_제거한다() {
        given(getGisuOrganizationUseCase.get(any())).willReturn(List.of(gisu(2L, 11L)));

        graphQlTester.document("""
                query {
                  gisuOrganizations(input: { generations: [11, 11, 12] }) {
                    gisus {
                      gisuId
                      generation
                    }
                  }
                }
                """)
            .execute()
            .path("gisuOrganizations.gisus[0].generation").entity(String.class).isEqualTo("11");

        ArgumentCaptor<GisuOrganizationQuery> captor = ArgumentCaptor.forClass(GisuOrganizationQuery.class);
        then(getGisuOrganizationUseCase).should().get(captor.capture());
        assertThat(captor.getValue().selector()).isEqualTo(GisuOrganizationQuery.Selector.GENERATION);
        assertThat(captor.getValue().generations()).containsExactly(11L, 12L);
    }

    @Test
    @DisplayName("기수 조직 조회는 active true selector를 전달한다")
    void 기수_조직_조회는_active_true_selector를_전달한다() {
        given(getGisuOrganizationUseCase.get(any())).willReturn(List.of(gisu(3L, 12L)));

        graphQlTester.document("""
                query {
                  gisuOrganizations(input: { active: true }) {
                    gisus {
                      active
                    }
                  }
                }
                """)
            .execute()
            .path("gisuOrganizations.gisus[0].active").entity(Boolean.class).isEqualTo(true);

        ArgumentCaptor<GisuOrganizationQuery> captor = ArgumentCaptor.forClass(GisuOrganizationQuery.class);
        then(getGisuOrganizationUseCase).should().get(captor.capture());
        assertThat(captor.getValue().selector()).isEqualTo(GisuOrganizationQuery.Selector.ACTIVE);
        assertThat(captor.getValue().active()).isTrue();
    }

    @Test
    @DisplayName("기수 조직 조회 기준이 없으면 GraphQL error를 반환한다")
    void 기수_조직_조회_기준이_없으면_GraphQL_error를_반환한다() {
        graphQlTester.document("""
                query {
                  gisuOrganizations(input: {}) {
                    gisus {
                      gisuId
                    }
                  }
                }
                """)
            .execute()
            .errors()
            .satisfy(errors -> assertThat(errors).hasSize(1));

        then(getGisuOrganizationUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기수 조직 조회 기준을 둘 이상 보내면 GraphQL error를 반환한다")
    void 기수_조직_조회_기준을_둘_이상_보내면_GraphQL_error를_반환한다() {
        graphQlTester.document("""
                query {
                  gisuOrganizations(input: { ids: [1], generations: [10] }) {
                    gisus {
                      gisuId
                    }
                  }
                }
                """)
            .execute()
            .errors()
            .satisfy(errors -> assertThat(errors).hasSize(1));

        then(getGisuOrganizationUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("active false는 GraphQL error를 반환한다")
    void active_false는_GraphQL_error를_반환한다() {
        graphQlTester.document("""
                query {
                  gisuOrganizations(input: { active: false }) {
                    gisus {
                      gisuId
                    }
                  }
                }
                """)
            .execute()
            .errors()
            .satisfy(errors -> assertThat(errors).hasSize(1));

        then(getGisuOrganizationUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기수 단건 조회는 GetGisuUseCase를 호출한다")
    void 기수_단건_조회는_GetGisuUseCase를_호출한다() {
        given(getGisuUseCase.getById(1L)).willReturn(gisuInfo(1L, 10L));

        graphQlTester.document("""
                query {
                  gisu(id: 1) {
                    gisuId
                    generation
                    active
                  }
                }
                """)
            .execute()
            .path("gisu.gisuId").entity(String.class).isEqualTo("1")
            .path("gisu.generation").entity(String.class).isEqualTo("10")
            .path("gisu.active").entity(Boolean.class).isEqualTo(true);

        then(getGisuUseCase).should().getById(1L);
    }

    @Test
    @DisplayName("활성 기수 조회는 GetGisuUseCase를 호출한다")
    void 활성_기수_조회는_GetGisuUseCase를_호출한다() {
        given(getGisuUseCase.getActiveGisu()).willReturn(gisuInfo(3L, 12L));

        graphQlTester.document("""
                query {
                  activeGisu {
                    gisuId
                    generation
                    active
                  }
                }
                """)
            .execute()
            .path("activeGisu.gisuId").entity(String.class).isEqualTo("3")
            .path("activeGisu.generation").entity(String.class).isEqualTo("12")
            .path("activeGisu.active").entity(Boolean.class).isEqualTo(true);

        then(getGisuUseCase).should().getActiveGisu();
    }

    @Test
    @DisplayName("전체 지부 조회와 지부 단건 조회는 GetChapterUseCase를 호출한다")
    void 전체_지부_조회와_지부_단건_조회는_GetChapterUseCase를_호출한다() {
        given(getChapterUseCase.getAllChapters()).willReturn(List.of(new ChapterInfo(1L, "Ain 지부")));
        given(getChapterUseCase.getChapterById(2L)).willReturn(new ChapterInfo(2L, "Ner 지부"));

        graphQlTester.document("""
                query {
                  chapters {
                    id
                    name
                  }
                  chapter(id: 2) {
                    id
                    name
                  }
                }
                """)
            .execute()
            .path("chapters[0].id").entity(String.class).isEqualTo("1")
            .path("chapters[0].name").entity(String.class).isEqualTo("Ain 지부")
            .path("chapter.id").entity(String.class).isEqualTo("2")
            .path("chapter.name").entity(String.class).isEqualTo("Ner 지부");

        then(getChapterUseCase).should().getAllChapters();
        then(getChapterUseCase).should().getChapterById(2L);
    }

    @Test
    @DisplayName("전체 학교 조회와 학교 단건 조회는 GetSchoolUseCase를 호출한다")
    void 전체_학교_조회와_학교_단건_조회는_GetSchoolUseCase를_호출한다() {
        given(getSchoolUseCase.getAllSchoolNames()).willReturn(List.of(new SchoolNameInfo(1L, "중앙대학교")));
        given(getSchoolUseCase.getSchoolDetail(2L)).willReturn(schoolDetail(2L, "동국대학교"));

        graphQlTester.document("""
                query {
                  schools {
                    schoolId
                    schoolName
                  }
                  school(id: 2) {
                    schoolId
                    schoolName
                    active
                    createdAt
                    updatedAt
                  }
                }
                """)
            .execute()
            .path("schools[0].schoolId").entity(String.class).isEqualTo("1")
            .path("schools[0].schoolName").entity(String.class).isEqualTo("중앙대학교")
            .path("school.schoolId").entity(String.class).isEqualTo("2")
            .path("school.schoolName").entity(String.class).isEqualTo("동국대학교")
            .path("school.active").entity(Boolean.class).isEqualTo(true);

        then(getSchoolUseCase).should().getAllSchoolNames();
        then(getSchoolUseCase).should().getSchoolDetail(2L);
    }

    private GisuOrganizationInfo gisuOrganization() {
        Instant createdAt = Instant.parse("2026-03-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-03-02T00:00:00Z");
        return new GisuOrganizationInfo(
            1L,
            10L,
            Instant.parse("2026-03-01T00:00:00Z"),
            Instant.parse("2026-08-31T23:59:59Z"),
            true,
            List.of(new ChapterOrganizationInfo(
                100L,
                "Ain 지부",
                List.of(new ChapterSchoolInfo(1000L, "중앙대학교"))
            )),
            List.of(new SchoolOrganizationInfo(
                100L,
                "Ain 지부",
                1000L,
                "중앙대학교",
                "비고",
                "https://storage.example.com/school-logo.png",
                List.of(new SchoolLinkInfo("인스타그램", SchoolLinkType.INSTAGRAM, "https://instagram.com/example")),
                true,
                createdAt,
                updatedAt
            ))
        );
    }

    private GisuOrganizationInfo gisu(Long gisuId, Long generation) {
        return new GisuOrganizationInfo(
            gisuId,
            generation,
            Instant.parse("2026-03-01T00:00:00Z"),
            Instant.parse("2026-08-31T23:59:59Z"),
            true,
            List.of(),
            List.of()
        );
    }

    private GisuInfo gisuInfo(Long gisuId, Long generation) {
        return new GisuInfo(
            gisuId,
            generation,
            Instant.parse("2026-03-01T00:00:00Z"),
            Instant.parse("2026-08-31T23:59:59Z"),
            true
        );
    }

    private SchoolDetailInfo schoolDetail(Long schoolId, String schoolName) {
        return new SchoolDetailInfo(
            10L,
            "Ain 지부",
            schoolName,
            schoolId,
            "비고",
            "https://storage.example.com/school-logo.png",
            List.of(new SchoolDetailInfo.SchoolLinkItem(
                "카카오톡",
                SchoolLinkType.KAKAO,
                "https://open.kakao.com/example"
            )),
            true,
            Instant.parse("2026-03-01T00:00:00Z"),
            Instant.parse("2026-03-02T00:00:00Z")
        );
    }
}
