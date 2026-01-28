package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.ChapterSchool;
import java.util.List;

public interface LoadChapterSchoolPort {

    List<ChapterSchool> findByGisuId(Long gisuId);
}
