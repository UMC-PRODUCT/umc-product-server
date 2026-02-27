package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.ChapterWithSchoolsInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GetChapterUseCase {

    List<ChapterInfo> getAllChapters();

    /**
     * 기수와 학교 정보로 지부 정보를 조회합니다.
     */
    ChapterInfo byGisuAndSchool(Long gisuId, Long schoolId);

    List<ChapterInfo> getChaptersBySchool(Long schoolId);

    List<ChapterWithSchoolsInfo> getChaptersWithSchoolsByGisuId(Long gisuId);

    ChapterInfo getChapterById(Long chapterId);

    /**
     * 여러 gisuId와 schoolId 조합에 해당하는 ChapterInfo를 1번 쿼리로 일괄 조회
     *
     * @return gisuId → (schoolId → ChapterInfo) 중첩 맵
     */
    Map<Long, Map<Long, ChapterInfo>> getChapterMapByGisuIdsAndSchoolIds(Set<Long> gisuIds, Set<Long> schoolIds);
}
