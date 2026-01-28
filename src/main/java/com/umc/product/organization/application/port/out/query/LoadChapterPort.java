package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.Chapter;
import java.util.List;

public interface LoadChapterPort {

    void validateExists(Long chapterId);

    Chapter findById(Long chapterId);

    List<Chapter> findAll();

    List<Chapter> findByGisuId(Long gisuId);
}
