package com.umc.product.organization.application.port.out;

import com.umc.product.organization.domain.Chapter;

public interface SaveChapterPort {


    Chapter save(Chapter chapter);

    void delete(Chapter chapter);
}
