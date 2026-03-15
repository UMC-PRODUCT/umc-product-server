package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo.SchoolInfoWithoutSchoolLinkItem;
import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.in.query.dto.UnassignedSchoolInfo;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.ChapterSchool;
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

        SchoolInfoWithoutSchoolLinkItem schoolInfoWithoutSchoolLinkItem = loadSchoolPort.findSchoolDetailByIdWithActiveChapter(schoolId);

        String logoImageUrl = null;
        if (schoolInfoWithoutSchoolLinkItem.logoImageId() != null) {
            FileInfo fileInfo = getFileUseCase.getById(schoolInfoWithoutSchoolLinkItem.logoImageId());
            logoImageUrl = fileInfo.fileLink();
        }

        List<SchoolDetailInfo.SchoolLinkItem> links = loadSchoolPort.findLinksBySchoolId(schoolId);

        return schoolInfoWithoutSchoolLinkItem.toDetailInfo(logoImageUrl, links);

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
        List<School> schools = loadSchoolPort.findSchoolsByGisuId(gisuId);

        List<String> logoImageIds = schools.stream()
            .map(School::getLogoImageId)
            .filter(Objects::nonNull)
            .toList();

        Map<String, String> logoImageUrls = logoImageIds.isEmpty()
            ? Map.of()
            : getFileUseCase.getFileLinks(logoImageIds);

        return schools.stream()
            .map(school -> toSchoolDetailInfo(school, gisuId, logoImageUrls.get(school.getLogoImageId())))
            .toList();
    }

    private SchoolDetailInfo toSchoolDetailInfo(School school, Long gisuId, String logoImageUrl) {
        ChapterSchool cs = school.getChapterSchools().stream()
            .filter(c -> c.getChapter().getGisu().getId().equals(gisuId))
            .findFirst()
            .orElse(null);

        List<SchoolDetailInfo.SchoolLinkItem> links = school.getSchoolLinks().stream()
            .map(link -> new SchoolDetailInfo.SchoolLinkItem(link.getTitle(), link.getType(), link.getUrl()))
            .toList();

        return new SchoolDetailInfo(
            cs != null ? cs.getChapter().getId() : null,
            cs != null ? cs.getChapter().getName() : null,
            school.getName(),
            school.getId(),
            school.getRemark(),
            logoImageUrl,
            links,
            cs != null && cs.getChapter().getGisu().isActive(),
            school.getCreatedAt(),
            school.getUpdatedAt()
        );
    }
}
