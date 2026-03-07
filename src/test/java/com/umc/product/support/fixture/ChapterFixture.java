package com.umc.product.support.fixture;

import com.umc.product.organization.application.port.out.command.ManageChapterPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import org.springframework.stereotype.Component;

@Component
public class ChapterFixture {

    private final ManageChapterPort manageChapterPort;

    public ChapterFixture(ManageChapterPort manageChapterPort) {
        this.manageChapterPort = manageChapterPort;
    }

    public Chapter 지부(Gisu gisu, String name) {
        return manageChapterPort.save(Chapter.create(gisu, name));
    }
}
