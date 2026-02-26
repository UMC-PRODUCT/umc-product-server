package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.in.query.dto.UnassignedSchoolInfo;
import com.umc.product.organization.application.port.out.query.LoadChapterSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.domain.School;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final LoadChapterSchoolPort loadChapterSchoolPort;
    private final GetFileUseCase getFileUseCase;


    @Override
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
        return loadSchoolPort.findAll().stream()
            .map(SchoolNameInfo::from)
            .toList();
    }

    @Override
    public SchoolDetailInfo getSchoolDetail(Long schoolId) {

        SchoolDetailInfo.SchoolInfo schoolInfo = loadSchoolPort.findSchoolDetailByIdWithActiveChapter(schoolId);

        String logoImageUrl = null;
        if (schoolInfo.logoImageId() != null) {
            FileInfo fileInfo = getFileUseCase.getById(schoolInfo.logoImageId());
            logoImageUrl = fileInfo.fileLink();
        }

        School school = loadSchoolPort.findById(schoolId);
        List<SchoolDetailInfo.SchoolLinkItem> links = school.getSchoolLinks().stream()
            .map(link -> new SchoolDetailInfo.SchoolLinkItem(link.getTitle(), link.getType(), link.getUrl()))
            .toList();

        return schoolInfo.toDetailInfo(logoImageUrl, links);

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

    // TODO: 여기는 심각하게 리팩토링이 필요해보인다

    @Override
    public List<SchoolDetailInfo> getSchoolListByGisuId(Long gisuId) {
        Set<School> schools = loadChapterSchoolPort.findByGisuId(gisuId)
            .stream()
            .map(ChapterSchool::getSchool)
            .collect(Collectors.toSet());

        return schools.stream()
            .map(school -> {
                SchoolDetailInfo.SchoolInfo schoolInfo = loadSchoolPort.findSchoolDetailByIdWithActiveChapter(
                    school.getId());

                String logoImageUrl = null;
                if (schoolInfo.logoImageId() != null) {
                    FileInfo fileInfo = getFileUseCase.getById(schoolInfo.logoImageId());
                    logoImageUrl = fileInfo.fileLink();
                }

                List<SchoolDetailInfo.SchoolLinkItem> links = school.getSchoolLinks().stream()
                    .map(link -> new SchoolDetailInfo.SchoolLinkItem(link.getTitle(), link.getType(), link.getUrl()))
                    .toList();

                return schoolInfo.toDetailInfo(logoImageUrl, links);
            }).toList();
    }
}
