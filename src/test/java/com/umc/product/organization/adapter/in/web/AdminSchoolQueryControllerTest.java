package com.umc.product.organization.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import com.umc.product.organization.domain.enums.SchoolLinkType;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.support.DocumentationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

class AdminSchoolQueryControllerTest extends DocumentationTest {

    @Test
    void 학교_목록을_조회합니다() throws Exception {
        // given
        int page = 0;
        int size = 10;

        List<SchoolListItemInfo> items = List.of(
            new SchoolListItemInfo(1L, "서울대학교", 1L, "Ain 지부", toInstant(2025, 12, 31), true, "비고1",
                "https://storage.example.com/logo1.png"),
            new SchoolListItemInfo(2L, "연세대학교", 1L, "Ain 지부", toInstant(2025, 12, 29), true, "비고2",
                "https://storage.example.com/logo2.png"),
            new SchoolListItemInfo(3L, "고려대학교", null, null, toInstant(2025, 12, 30), false, null, null)
        );

        Page<SchoolListItemInfo> pageResult = new PageImpl<>(
            items,
            PageRequest.of(page, size),
            3L
        );

        given(getSchoolUseCase.getSchools(any(), any())).willReturn(pageResult);

        // when
        ResultActions result = mockMvc.perform(
            get("/api/v1/schools")
                .param("keyword", "중앙대학교")
                .param("chapterId", "1")
                .param("page", "0")
                .param("size", String.valueOf(size))
        );

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                queryParameters(
                    parameterWithName("keyword").description("학교 이름 검색 키워드").optional(),
                    parameterWithName("chapterId").description("지부 ID").optional(),
                    parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
                    parameterWithName("size").description("페이지 당 조회 수").optional()
                ),
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.content").type(JsonFieldType.ARRAY).description("학교 목록"),
                    fieldWithPath("result.content[].schoolId").type(JsonFieldType.STRING)
                        .description("학교 ID"),
                    fieldWithPath("result.content[].schoolName").type(JsonFieldType.STRING)
                        .description("학교 이름"),
                    fieldWithPath("result.content[].chapterId").type(JsonFieldType.STRING)
                        .description("지부 ID (활성 기수에 속하지 않으면 null)").optional(),
                    fieldWithPath("result.content[].chapterName").type(JsonFieldType.STRING)
                        .description("지부 이름 (활성 기수에 속하지 않으면 null)").optional(),
                    fieldWithPath("result.content[].createdAt").type(JsonFieldType.STRING)
                        .description("등록일"),
                    fieldWithPath("result.content[].isActive").type(JsonFieldType.BOOLEAN)
                        .description("활성 상태"),
                    fieldWithPath("result.content[].remark").type(JsonFieldType.STRING)
                        .description("비고").optional(),
                    fieldWithPath("result.content[].logoImageUrl").type(JsonFieldType.STRING)
                        .description("학교 로고 이미지 URL").optional(),
                    fieldWithPath("result.page").type(JsonFieldType.STRING)
                        .description("현재 페이지 번호 (0부터 시작)"),
                    fieldWithPath("result.size").type(JsonFieldType.STRING).description("페이지 당 조회 수"),
                    fieldWithPath("result.totalElements").type(JsonFieldType.STRING).description("총 학교 수"),
                    fieldWithPath("result.totalPages").type(JsonFieldType.STRING).description("총 페이지 수"),
                    fieldWithPath("result.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
                    fieldWithPath("result.hasPrevious").type(JsonFieldType.BOOLEAN)
                        .description("이전 페이지 존재 여부")
                )
            ));
    }

    @Test
    void 학교_상세정보를_조회합니다() throws Exception {
        // give
        Long schoolId = 1L;
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant updatedAt = Instant.parse("2026-01-04T00:00:00Z");

        List<SchoolDetailInfo.SchoolLinkItem> links = List.of(
            new SchoolDetailInfo.SchoolLinkItem("카카오톡 오픈채팅", SchoolLinkType.KAKAO, "https://open.kakao.com/o/example"),
            new SchoolDetailInfo.SchoolLinkItem("인스타그램", SchoolLinkType.INSTAGRAM, "https://instagram.com/example"),
            new SchoolDetailInfo.SchoolLinkItem("유튜브 채널", SchoolLinkType.YOUTUBE, "https://youtube.com/@example")
        );

        SchoolDetailInfo schoolDetailInfo = new SchoolDetailInfo(3L, "Ain 지부", "중앙대학교", 1L, "비고", "logo-file-123",
            links, true, createdAt, updatedAt);
        FileInfo fileInfo = new FileInfo("logo-file-123", "동국대학교 로고", FileCategory.SCHOOL_LOGO, null, null,
            "https://storage.example.com/school-logo/logo.png", null, null, null);
        given(getSchoolUseCase.getSchoolDetail(schoolId)).willReturn(schoolDetailInfo);
        given(getFileUseCase.getById("logo-file-123")).willReturn(fileInfo);
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/schools/{schoolId}", schoolId));
        // then
        result.andExpect((status().isOk()))
            .andDo(restDocsHandler.document(pathParameters(parameterWithName("schoolId").description("학교 ID")),
                responseFields(fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.chapterId").type(JsonFieldType.STRING).description("지부 ID"),
                    fieldWithPath("result.chapterName").type(JsonFieldType.STRING).description("지부 이름"),
                    fieldWithPath("result.schoolName").type(JsonFieldType.STRING).description("학교 이름"),
                    fieldWithPath("result.schoolId").type(JsonFieldType.STRING).description("학교 ID"),
                    fieldWithPath("result.isActive").type(JsonFieldType.BOOLEAN).description("학교 활성상태"),
                    fieldWithPath("result.remark").type(JsonFieldType.STRING).description("비고"),
                    fieldWithPath("result.logoImageUrl").type(JsonFieldType.STRING).description("로고 이미지 URL")
                        .optional(),
                    fieldWithPath("result.links").type(JsonFieldType.ARRAY).description("학교 링크 목록"),
                    fieldWithPath("result.links[].title").type(JsonFieldType.STRING).description("링크 제목"),
                    fieldWithPath("result.links[].type").type(JsonFieldType.STRING)
                        .description("링크 타입 (KAKAO, INSTAGRAM, YOUTUBE)"),
                    fieldWithPath("result.links[].url").type(JsonFieldType.STRING).description("링크 URL"),
                    fieldWithPath("result.createdAt").type(JsonFieldType.STRING).description("생성일자"),
                    fieldWithPath("result.updatedAt").type(JsonFieldType.STRING).description("수정일자"))));
    }

    @Test
    void 학교_전체_목록을_조회합니다() throws Exception {
        // given
        List<SchoolNameInfo> schoolNames = List.of(
            new SchoolNameInfo(1L, "동국대학교"),
            new SchoolNameInfo(2L, "서울대학교"),
            new SchoolNameInfo(3L, "중앙대학교")
        );

        given(getSchoolUseCase.getAllSchoolNames()).willReturn(schoolNames);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/schools/all"));

        // then
        result.andExpect(status().isOk())
            .andDo(restDocsHandler.document(
                responseFields(
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
                    fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("result.schools").type(JsonFieldType.ARRAY).description("학교 목록"),
                    fieldWithPath("result.schools[].schoolId").type(JsonFieldType.STRING).description("학교 ID"),
                    fieldWithPath("result.schools[].schoolName").type(JsonFieldType.STRING).description("학교 이름")
                )
            ));
    }

    private Instant toInstant(int year, int month, int day) {
        return Instant.parse(String.format("%04d-%02d-%02dT00:00:00Z", year, month, day));
    }

}
