package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.in.query.dto.UnassignedSchoolInfo;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.School;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolQueryService implements GetSchoolUseCase {


    private final LoadSchoolPort loadSchoolPort;
    private final GetFileUseCase getFileUseCase;


    @Override
    @Deprecated
    public Page<SchoolListItemInfo> getSchools(SchoolSearchCondition condition, Pageable pageable) {
        Page<SchoolListItemInfo> page = loadSchoolPort.findSchools(condition, pageable);

        List<String> logoImageIds = page.getContent().stream()
            .map(SchoolListItemInfo::logoImageUrl)
            .filter(Objects::nonNull)
            .toList();

        if (logoImageIds.isEmpty()) {
            return page;
        }

        Map<String, String> fileLinks = getFileUseCase.getFileLinks(logoImageIds);

        return page.map(info -> {
            if (info.logoImageUrl() == null) {
                return info;
            }
            return info.withLogoImageUrl(fileLinks.get(info.logoImageUrl()));
        });
    }

    @Override
    public List<SchoolNameInfo> getAllSchoolNames() {
        return loadSchoolPort.findAllNames();
    }

    @Override
    public SchoolDetailInfo getSchoolDetail(Long schoolId) {

        SchoolChapterInfo schoolInfoWithoutSchoolLinkItem = loadSchoolPort.findSchoolDetailByIdWithActiveChapter(schoolId);

        String logoImageUrl = null;
        if (schoolInfoWithoutSchoolLinkItem.logoImageId() != null) {
            FileInfo fileInfo = getFileUseCase.getById(schoolInfoWithoutSchoolLinkItem.logoImageId());
            logoImageUrl = fileInfo.fileLink();
        }

        List<SchoolDetailInfo.SchoolLinkItem> links = loadSchoolPort.findLinksBySchoolId(schoolId);

        return toSchoolDetailInfo(schoolInfoWithoutSchoolLinkItem, logoImageUrl, links);

    }

    @Override
    public SchoolLinkInfo getSchoolLink(Long schoolId) {

        School school = loadSchoolPort.findById(schoolId);

        return SchoolLinkInfo.from(school);

    }

    @Override
    public List<UnassignedSchoolInfo> getUnassignedSchools(Long gisuId) {
        return loadSchoolPort.findUnassignedByGisuId(gisuId).stream()
            .map(UnassignedSchoolInfo::from)
            .toList();
    }

    @Override
    public List<SchoolDetailInfo> getSchoolListByGisuId(Long gisuId) {

        List<SchoolChapterInfo> schools = loadSchoolPort.findSchoolDetailsByGisuId(gisuId);
        if (schools.isEmpty()) return List.of();

        List<Long> schoolIds = schools.stream().map(SchoolChapterInfo::schoolId).toList();

        Map<Long, List<SchoolDetailInfo.SchoolLinkItem>> linksMap = loadSchoolPort.findLinksBySchoolIds(schoolIds);

        List<String> logoImageIds = schools.stream()
            .map(SchoolChapterInfo::logoImageId)
            .filter(Objects::nonNull)
            .toList();

        Map<String, String> logoImageUrls = logoImageIds.isEmpty()
            ? Map.of()
            : getFileUseCase.getFileLinks(logoImageIds);

        return schools.stream()
            .map(school -> toSchoolDetailInfo(
                school,
                logoImageUrls.get(school.logoImageId()),
                linksMap.getOrDefault(school.schoolId(), List.of())
            ))
            .toList();
    }

    private SchoolDetailInfo toSchoolDetailInfo(SchoolChapterInfo info, String logoImageUrl, List<SchoolDetailInfo.SchoolLinkItem> links) {
        return new SchoolDetailInfo(
            info.chapterId(),
            info.chapterName(),
            info.schoolName(),
            info.schoolId(),
            info.remark(),
            logoImageUrl,
            links,
            info.isActive(),
            info.createdAt(),
            info.updatedAt()
        );
    }
}
