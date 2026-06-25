package com.umc.product.organization.adapter.in.graphql.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo;

public record GisuOrganizationPayloadGraphQlResponse(
    List<GisuOrganizationGraphQlResponse> gisus
) {

    public static GisuOrganizationPayloadGraphQlResponse from(List<GisuOrganizationInfo> infos) {
        return new GisuOrganizationPayloadGraphQlResponse(
            infos.stream()
                .map(GisuOrganizationGraphQlResponse::from)
                .toList()
        );
    }

    public record GisuOrganizationGraphQlResponse(
        Long gisuId,
        Long generation,
        String startAt,
        String endAt,
        boolean active,
        List<ChapterGraphQlResponse> chapters,
        List<SchoolDetailGraphQlResponse> schools
    ) {

        public static GisuOrganizationGraphQlResponse from(GisuOrganizationInfo info) {
            return new GisuOrganizationGraphQlResponse(
                info.gisuId(),
                info.generation(),
                format(info.startAt()),
                format(info.endAt()),
                info.isActive(),
                info.chapters().stream()
                    .map(ChapterGraphQlResponse::from)
                    .toList(),
                info.schools().stream()
                    .map(SchoolDetailGraphQlResponse::from)
                    .toList()
            );
        }
    }

    public record ChapterGraphQlResponse(
        Long chapterId,
        String chapterName,
        List<ChapterSchoolGraphQlResponse> schools
    ) {

        public static ChapterGraphQlResponse from(GisuOrganizationInfo.ChapterOrganizationInfo info) {
            return new ChapterGraphQlResponse(
                info.chapterId(),
                info.chapterName(),
                info.schools().stream()
                    .map(ChapterSchoolGraphQlResponse::from)
                    .toList()
            );
        }
    }

    public record ChapterSchoolGraphQlResponse(
        Long schoolId,
        String schoolName
    ) {

        public static ChapterSchoolGraphQlResponse from(GisuOrganizationInfo.ChapterSchoolInfo info) {
            return new ChapterSchoolGraphQlResponse(info.schoolId(), info.schoolName());
        }
    }

    private static String format(Instant instant) {
        return instant == null ? null : instant.toString();
    }
}
