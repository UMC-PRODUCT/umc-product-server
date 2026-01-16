package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.Chapter;

public interface LoadChapterPort {

    void existsById(Long chapterId);

    Chapter findById(Long chapterId);

}
