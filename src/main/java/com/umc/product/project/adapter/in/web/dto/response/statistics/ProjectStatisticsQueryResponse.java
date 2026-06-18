package com.umc.product.project.adapter.in.web.dto.response.statistics;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 프로젝트 통계 통합 조회 응답 marker interface.
 */
@Schema(oneOf = {
    ProjectStatisticsResponse.class,
    ChapterProjectStatisticsResponse.class
})
public interface ProjectStatisticsQueryResponse {
}
