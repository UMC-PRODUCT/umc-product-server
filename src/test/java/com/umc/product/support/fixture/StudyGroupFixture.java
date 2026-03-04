package com.umc.product.support.fixture;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.out.command.ManageStudyGroupPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.StudyGroup;
import org.springframework.stereotype.Component;

@Component
public class StudyGroupFixture {

    private final ManageStudyGroupPort manageStudyGroupPort;

    public StudyGroupFixture(ManageStudyGroupPort manageStudyGroupPort) {
        this.manageStudyGroupPort = manageStudyGroupPort;
    }

    public StudyGroup 스터디그룹(String name, Gisu gisu, ChallengerPart part, Long leaderId, Long... memberIds) {
        StudyGroup studyGroup = StudyGroup.create(name, gisu, part);
        studyGroup.addMember(leaderId, true);
        for (Long memberId : memberIds) {
            studyGroup.addMember(memberId);
        }
        return manageStudyGroupPort.save(studyGroup);
    }

    public StudyGroup 스터디그룹(String name, Gisu gisu, ChallengerPart part) {
        return manageStudyGroupPort.save(StudyGroup.create(name, gisu, part));
    }
}
