package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.ChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.ChapterWithSchoolsInfo;
import java.util.List;

public interface GetChapterUseCase {

    List<ChapterInfo> getAllChapters();

    List<ChapterWithSchoolsInfo> getChaptersWithSchoolsByGisuId(Long gisuId);
}
