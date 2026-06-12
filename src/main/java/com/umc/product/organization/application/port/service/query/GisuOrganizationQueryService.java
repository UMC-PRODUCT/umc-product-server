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
import java.util.List;
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
        return getGisus(query).stream()
            .map(gisu -> GisuOrganizationInfo.of(
                gisu,
                getChapters(gisu.gisuId(), query),
                getSchools(gisu.gisuId(), query)
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

    private List<ChapterOrganizationInfo> getChapters(Long gisuId, GisuOrganizationQuery query) {
        if (!query.includeChapter()) {
            return List.of();
        }

        if (!query.includeSchool()) {
            return getChapterUseCase.listByGisuId(gisuId).stream()
                .map(ChapterOrganizationInfo::from)
                .toList();
        }

        return getChapterUseCase.getChaptersWithSchoolsByGisuId(gisuId).stream()
            .map(ChapterOrganizationInfo::from)
            .toList();
    }

    private List<SchoolOrganizationInfo> getSchools(Long gisuId, GisuOrganizationQuery query) {
        if (!query.includeSchool()) {
            return List.of();
        }

        List<SchoolDetailInfo> schools = getSchoolUseCase.getSchoolListByGisuId(gisuId);
        return schools.stream()
            .map(SchoolOrganizationInfo::from)
            .toList();
    }
}
