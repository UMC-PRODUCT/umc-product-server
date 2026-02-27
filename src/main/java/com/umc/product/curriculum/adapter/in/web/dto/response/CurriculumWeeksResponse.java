package com.umc.product.curriculum.adapter.in.web.dto.response;

import com.umc.product.curriculum.application.port.in.query.CurriculumWeekInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "커리큘럼 주차 목록")
public record CurriculumWeeksResponse(
        @Schema(description = "주차 목록")
        List<WeekInfo> weeks
) {

    public static CurriculumWeeksResponse from(List<CurriculumWeekInfo> infos) {
        return new CurriculumWeeksResponse(
                infos.stream()
                        .map(WeekInfo::from)
                        .toList()
        );
    }

    @Schema(description = "주차 정보")
    public record WeekInfo(
            @Schema(description = "주차 번호", example = "1")
            Integer weekNo,

            @Schema(description = "워크북 제목", example = "1주차 - Spring Boot 시작하기")
            String title
    ) {

        public static WeekInfo from(CurriculumWeekInfo info) {
            return new WeekInfo(info.weekNo(), info.title());
        }
    }
}
