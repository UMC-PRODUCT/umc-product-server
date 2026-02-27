package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.ChapterWithSchoolsInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "지부 및 소속 학교 목록 응답")
public record ChapterWithSchoolsResponse(
        @Schema(description = "지부 목록")
        List<ChapterWithSchools> chapters
) {

    public static ChapterWithSchoolsResponse from(List<ChapterWithSchoolsInfo> infos) {
        List<ChapterWithSchools> chapters = infos.stream()
                .map(ChapterWithSchools::from)
                .toList();
        return new ChapterWithSchoolsResponse(chapters);
    }

    @Schema(description = "지부 및 소속 학교 정보")
    public record ChapterWithSchools(
            @Schema(description = "지부 ID", example = "1")
            Long chapterId,
            @Schema(description = "지부명", example = "서울")
            String chapterName,
            @Schema(description = "소속 학교 목록")
            List<SchoolItem> schools
    ) {

        public static ChapterWithSchools from(ChapterWithSchoolsInfo info) {
            List<SchoolItem> schools = info.schools().stream()
                    .map(s -> new SchoolItem(s.schoolId(), s.schoolName()))
                    .toList();
            return new ChapterWithSchools(info.chapterId(), info.chapterName(), schools);
        }
    }

    @Schema(description = "학교 정보")
    public record SchoolItem(
            @Schema(description = "학교 ID", example = "1")
            Long schoolId,
            @Schema(description = "학교명", example = "서울대학교")
            String schoolName
    ) {
    }
}
