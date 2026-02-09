package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.Chapter;

public interface ManageChapterPort {

    Chapter save(Chapter chapter);

    void delete(Chapter chapter);
}
