package com.umc.product.support.fixture;

import com.umc.product.organization.application.port.out.command.SaveChapterPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import org.springframework.stereotype.Component;

@Component
public class ChapterFixture {

    private final SaveChapterPort saveChapterPort;

    public ChapterFixture(SaveChapterPort saveChapterPort) {
        this.saveChapterPort = saveChapterPort;
    }

    public Chapter 지부(Gisu gisu, String name) {
        return saveChapterPort.save(Chapter.create(gisu, name));
    }
}
