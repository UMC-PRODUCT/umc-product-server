package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuOrganizationUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo.ChapterOrganizationInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo.SchoolOrganizationInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationQuery;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GisuOrganizationQueryService implements GetGisuOrganizationUseCase {

    private final GetGisuUseCase getGisuUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetSchoolUseCase getSchoolUseCase;

    @Override
    public List<GisuOrganizationInfo> get(GisuOrganizationQuery query) {
        List<GisuInfo> gisus = getGisus(query);
        Set<Long> gisuIds = gisus.stream()
            .map(GisuInfo::gisuId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, List<ChapterOrganizationInfo>> chaptersByGisuId = getChapters(gisuIds, query);
        Map<Long, List<SchoolOrganizationInfo>> schoolsByGisuId = getSchools(gisuIds, query);

        return gisus.stream()
            .map(gisu -> GisuOrganizationInfo.of(
                gisu,
                chaptersByGisuId.getOrDefault(gisu.gisuId(), List.of()),
                schoolsByGisuId.getOrDefault(gisu.gisuId(), List.of())
            ))
            .toList();
    }

    private List<GisuInfo> getGisus(GisuOrganizationQuery query) {
        return switch (query.selector()) {
            case ID -> getGisuUseCase.batchGetByIds(query.ids());
            case GENERATION -> getGisuUseCase.batchGetByGenerations(query.generations());
            case ACTIVE -> List.of(getGisuUseCase.getActiveGisu());
        };
    }

    private Map<Long, List<ChapterOrganizationInfo>> getChapters(Set<Long> gisuIds, GisuOrganizationQuery query) {
        if (!query.includeChapter() || gisuIds.isEmpty()) {
            return Map.of();
        }

        if (!query.includeSchool()) {
            return getChapterUseCase.listByGisuIds(gisuIds).entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().stream()
                        .map(ChapterOrganizationInfo::from)
                        .toList()
                ));
        }

        return getChapterUseCase.getChaptersWithSchoolsByGisuIds(gisuIds).entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(ChapterOrganizationInfo::from)
                    .toList()
            ));
    }

    private Map<Long, List<SchoolOrganizationInfo>> getSchools(Set<Long> gisuIds, GisuOrganizationQuery query) {
        if (!query.includeSchool() || gisuIds.isEmpty()) {
            return Map.of();
        }

        return getSchoolUseCase.getSchoolListByGisuIds(gisuIds).entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(SchoolOrganizationInfo::from)
                    .toList()
            ));
    }
}
