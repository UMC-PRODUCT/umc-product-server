package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.ChapterWithSchoolsInfo;
import java.util.List;

public interface GetChapterUseCase {

    List<ChapterInfo> getAllChapters();

    /**
     * 기수와 학교 정보로 지부 정보를 조회합니다.
     */
    ChapterInfo byGisuAndSchool(Long gisuId, Long schoolId);

    List<ChapterInfo> getChaptersBySchool(Long schoolId);

    List<ChapterWithSchoolsInfo> getChaptersWithSchoolsByGisuId(Long gisuId);
}
