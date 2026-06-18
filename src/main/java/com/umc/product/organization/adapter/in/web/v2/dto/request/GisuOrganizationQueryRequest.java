package com.umc.product.organization.adapter.in.web.v2.dto.request;

import java.util.List;
import java.util.Objects;

import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationQuery;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기수 조직 조회 조건")
public record GisuOrganizationQueryRequest(
    @Schema(description = "기수 ID 목록. 중복 값은 제거됩니다.", example = "1")
    List<Long> id,

    @Schema(description = "기수 번호 목록. 중복 값은 제거됩니다.", example = "10")
    List<Long> generation,

    @Schema(description = "활성 기수만 조회할지 여부. true만 허용", example = "true")
    Boolean active,

    @Schema(description = "기수 내 지부 정보 포함 여부", example = "false")
    Boolean includeChapter,

    @Schema(description = "기수 내 학교 정보 포함 여부", example = "false")
    Boolean includeSchool
) {

    public GisuOrganizationQuery toQuery() {
        List<Long> uniqueIds = unique(id);
        List<Long> uniqueGenerations = unique(generation);

        if (Boolean.FALSE.equals(active)) {
            throw invalidCondition();
        }

        boolean hasId = !uniqueIds.isEmpty();
        boolean hasGeneration = !uniqueGenerations.isEmpty();
        boolean hasActive = Boolean.TRUE.equals(active);

        int selectorCount = countSelected(hasId, hasGeneration, hasActive);
        if (selectorCount != 1) {
            throw invalidCondition();
        }

        boolean chapterIncluded = Boolean.TRUE.equals(includeChapter);
        boolean schoolIncluded = Boolean.TRUE.equals(includeSchool);

        if (hasId) {
            return GisuOrganizationQuery.byIds(uniqueIds, chapterIncluded, schoolIncluded);
        }
        if (hasGeneration) {
            return GisuOrganizationQuery.byGenerations(uniqueGenerations, chapterIncluded, schoolIncluded);
        }
        return GisuOrganizationQuery.active(chapterIncluded, schoolIncluded);
    }

    private List<Long> unique(List<Long> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private int countSelected(boolean hasId, boolean hasGeneration, boolean hasActive) {
        int count = 0;
        if (hasId) {
            count++;
        }
        if (hasGeneration) {
            count++;
        }
        if (hasActive) {
            count++;
        }
        return count;
    }

    private OrganizationDomainException invalidCondition() {
        return new OrganizationDomainException(OrganizationErrorCode.GISU_QUERY_CONDITION_INVALID);
    }
}
