package com.umc.product.support.fixture;

import com.umc.product.organization.application.port.out.command.SaveSchoolPort;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.School;
import org.springframework.stereotype.Component;

@Component
public class SchoolFixture {

    private final SaveSchoolPort saveSchoolPort;

    public SchoolFixture(SaveSchoolPort saveSchoolPort) {
        this.saveSchoolPort = saveSchoolPort;
    }

    public School 학교(String name) {
        return saveSchoolPort.save(School.create(name, null));
    }

    public School 지부에_소속된_학교(String name, Chapter chapter) {
        School school = School.create(name, null);
        school.assignToChapter(chapter);
        return saveSchoolPort.save(school);
    }
}
