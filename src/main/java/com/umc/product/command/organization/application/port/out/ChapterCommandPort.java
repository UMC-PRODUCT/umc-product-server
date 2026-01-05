package com.umc.product.command.organization.application.port.out;

import com.umc.product.command.organization.domain.Chapter;

public interface ChapterCommandPort {


    Chapter save(Chapter chapter);

    void delete(Chapter chapter);
}
