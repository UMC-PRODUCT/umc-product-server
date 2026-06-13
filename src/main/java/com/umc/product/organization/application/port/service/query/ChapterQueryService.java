package com.umc.product.organization.application.port.service.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.out.query.LoadChapterPort;
import com.umc.product.organization.application.port.out.query.LoadChapterSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterQueryService implements GetChapterUseCase {

    private final LoadChapterPort loadChapterPort;
    private final LoadChapterSchoolPort loadChapterSchoolPort;

    @Override
    public List<ChapterInfo> getAllChapters() {

        return loadChapterPort.findAll().stream().map(ChapterInfo::from).toList();

    }

    @Override
    public List<ChapterInfo> listByGisuId(Long gisuId) {
        return loadChapterPort.findByGisuId(gisuId).stream()
            .map(ChapterInfo::from)
            .toList();
    }

    @Override
    public Map<Long, List<ChapterInfo>> listByGisuIds(Set<Long> gisuIds) {
        if (gisuIds.isEmpty()) {
            return Map.of();
        }

        return loadChapterPort.findByGisuIds(gisuIds).stream()
            .collect(Collectors.groupingBy(
                chapter -> chapter.getGisu().getId(),
                Collectors.mapping(ChapterInfo::from, Collectors.toList())
            ));
    }

    @Override
    public ChapterInfo byGisuAndSchool(Long gisuId, Long schoolId) {
        // 지부 정보를 보여줘야 하면 ChapterSchool을 봐야 함
        // 전체 ChapterSchool 중에서 schoolId에 따라서 필터링하고
        // ChapterSchool 중에서 gisuId가 일치하는 chapter를 반환하면 됨

        List<ChapterSchool> chapterSchools = loadChapterSchoolPort.findBySchoolId(schoolId);
        for (ChapterSchool chapterSchool : chapterSchools) {
            Chapter chapter = chapterSchool.getChapter();
            if (chapter.getGisu().getId().equals(gisuId)) {
                return ChapterInfo.from(chapter);
            }
        }

        throw new OrganizationDomainException(OrganizationErrorCode.CHAPTER_NOT_FOUND);
    }

    @Override
    public List<ChapterInfo> getChaptersBySchool(Long schoolId) {
        // 지부별 학교 정보를 학교 ID로 전부 먼저 가져오고
        List<ChapterSchool> chapterSchools = loadChapterSchoolPort.findBySchoolId(schoolId);

        // 거기서 지부 정보를 매핑해서 List 형태로 반환함
        return chapterSchools.stream()
            .map(ChapterSchool::getChapter)
            .map(ChapterInfo::from)
            .toList();
    }

    @Override
    public List<ChapterWithSchoolsInfo> getChaptersWithSchoolsByGisuId(Long gisuId) {
        List<Chapter> chapters = loadChapterPort.findByGisuId(gisuId);
        List<ChapterSchool> chapterSchools = loadChapterSchoolPort.findByGisuId(gisuId);

        // ChapterSchool에 Chapter와 chapters를 Mapping
        Map<Long, List<ChapterSchool>> chapterSchoolMap = chapterSchools.stream()
            .collect(Collectors.groupingBy(cs -> cs.getChapter().getId()));

        return chapters.stream()
            .map(chapter -> ChapterWithSchoolsInfo.from(
                chapter,
                chapterSchoolMap.getOrDefault(chapter.getId(), List.of())
            ))
            .toList();
    }

    @Override
    public Map<Long, List<ChapterWithSchoolsInfo>> getChaptersWithSchoolsByGisuIds(Set<Long> gisuIds) {
        if (gisuIds.isEmpty()) {
            return Map.of();
        }

        List<Chapter> chapters = loadChapterPort.findByGisuIds(gisuIds);
        List<ChapterSchool> chapterSchools = loadChapterSchoolPort.findByGisuIds(gisuIds);

        Map<Long, List<ChapterSchool>> chapterSchoolMap = chapterSchools.stream()
            .collect(Collectors.groupingBy(cs -> cs.getChapter().getId()));

        return chapters.stream()
            .collect(Collectors.groupingBy(
                chapter -> chapter.getGisu().getId(),
                Collectors.mapping(
                    chapter -> ChapterWithSchoolsInfo.from(
                        chapter,
                        chapterSchoolMap.getOrDefault(chapter.getId(), List.of())
                    ),
                    Collectors.toList()
                )
            ));
    }

    @Override
    public ChapterInfo getChapterById(Long chapterId) {
        return ChapterInfo.from(loadChapterPort.findById(chapterId));
    }

    @Override
    public Map<Long, Map<Long, ChapterInfo>> getChapterMapByGisuIdsAndSchoolIds(
        Set<Long> gisuIds, Set<Long> schoolIds
    ) {
        if (gisuIds.isEmpty() || schoolIds.isEmpty()) {
            return Map.of();
        }

        List<ChapterSchool> chapterSchools =
            loadChapterSchoolPort.findByGisuIdsAndSchoolIds(gisuIds, schoolIds);

        // chapter, chapter.gisu, school이 fetch join으로 이미 로드되어 있음
        Map<Long, Map<Long, ChapterInfo>> result = new HashMap<>();
        for (ChapterSchool cs : chapterSchools) {
            Long gisuId = cs.getChapter().getGisu().getId();
            Long schoolId = cs.getSchool().getId();
            result.computeIfAbsent(gisuId, k -> new HashMap<>())
                .put(schoolId, ChapterInfo.from(cs.getChapter()));
        }
        return result;
    }
}
