package com.umc.product.support.fixture;

import com.umc.product.authorization.application.port.out.SaveChallengerRolePort;
import com.umc.product.authorization.domain.ChallengerRole;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import org.springframework.stereotype.Component;

@Component
public class ChallengerRoleFixture extends FixtureSupport {

    private final SaveChallengerRolePort saveChallengerRolePort;

    public ChallengerRoleFixture(SaveChallengerRolePort saveChallengerRolePort) {
        this.saveChallengerRolePort = saveChallengerRolePort;
    }

    public ChallengerRole centralPresident(Long challengerId, Long gisuId) {
        return saveChallengerRolePort.save(
            ChallengerRole.create(challengerId, ChallengerRoleType.CENTRAL_PRESIDENT, null, null, gisuId));
    }

    public ChallengerRole viceCentralPresident(Long challengerId, Long gisuId) {
        return saveChallengerRolePort.save(
            ChallengerRole.create(challengerId, ChallengerRoleType.CENTRAL_VICE_PRESIDENT, null, null, gisuId));
    }

    public ChallengerRole centralMember(Long challengerId, Long gisuId) {
        return saveChallengerRolePort.save(
            ChallengerRole.create(challengerId, ChallengerRoleType.CENTRAL_OPERATING_TEAM_MEMBER, null, null, gisuId));
    }

    // 지부

    public ChallengerRole chapterLead(Long challengerId, Long chapterId, Long gisuId) {
        return saveChallengerRolePort.save(
            ChallengerRole.create(challengerId, ChallengerRoleType.CHAPTER_PRESIDENT, chapterId, null, gisuId));
    }

    // 학교

    public ChallengerRole schoolPresident(Long challengerId, Long schoolId, Long gisuId) {
        return saveChallengerRolePort.save(
            ChallengerRole.create(challengerId, ChallengerRoleType.SCHOOL_PRESIDENT, schoolId, null, gisuId));
    }

    public ChallengerRole schoolVicePresident(Long challengerId, Long schoolId, Long gisuId) {
        return saveChallengerRolePort.save(
            ChallengerRole.create(challengerId, ChallengerRoleType.SCHOOL_VICE_PRESIDENT, schoolId, null, gisuId));
    }

    public ChallengerRole schoolPartLead(Long challengerId, ChallengerPart part, Long schoolId, Long gisuId) {
        return saveChallengerRolePort.save(
            ChallengerRole.create(challengerId, ChallengerRoleType.SCHOOL_PART_LEADER, schoolId, part, gisuId));
    }

    public ChallengerRole schoolEtcAdmin(Long challengerId, ChallengerPart part, Long schoolId, Long gisuId) {
        return saveChallengerRolePort.save(
            ChallengerRole.create(challengerId, ChallengerRoleType.SCHOOL_ETC_ADMIN, schoolId, part, gisuId));
    }
}
