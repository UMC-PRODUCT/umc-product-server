package com.umc.product.support.fixture;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.out.command.SaveStudyGroupPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.StudyGroup;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StudyGroupFixture extends FixtureSupport {

    private final SaveStudyGroupPort saveStudyGroupPort;

    public StudyGroupFixture(SaveStudyGroupPort saveStudyGroupPort) {
        this.saveStudyGroupPort = saveStudyGroupPort;
    }

    public StudyGroup 스터디그룹(String name, Gisu gisu, ChallengerPart part) {
        return saveStudyGroupPort.save(StudyGroup.create(
            valueOrFixture(name, "study-group", 100),
            gisu.getId(),
            part
        ));
    }

    public StudyGroup 스터디그룹(Gisu gisu, ChallengerPart part) {
        return 스터디그룹(fixtureString("study-group", 100), gisu, part);
    }

    public StudyGroup 스터디그룹(
        String name, Gisu gisu, ChallengerPart part,
        Set<Long> mentorIds,
        Set<Long> memberIds
    ) {
        Set<Long> mentors = mentorIds == null ? Set.of() : mentorIds;
        Set<Long> members = memberIds == null ? Set.of() : memberIds;

        StudyGroup group = StudyGroup.create(
            valueOrFixture(name, "study-group", 100),
            gisu.getId(),
            part
        );
        if (!members.isEmpty()) {
            group.addMembers(members);
        }
        if (!mentors.isEmpty()) {
            group.assignMentors(mentors);
        }
        return saveStudyGroupPort.save(group);
    }
}
