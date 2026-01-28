package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.ChapterWithSchoolsInfo;
import com.umc.product.organization.application.port.out.query.LoadChapterPort;
import com.umc.product.organization.application.port.out.query.LoadChapterSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.ChapterSchool;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
