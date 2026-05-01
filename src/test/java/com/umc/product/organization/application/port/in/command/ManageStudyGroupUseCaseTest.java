package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.support.UseCaseTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.MemberFixture;
import com.umc.product.support.fixture.StudyGroupFixture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ManageStudyGroupUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ManageStudyGroupUseCase manageStudyGroupUseCase;

    @Autowired
    private GisuFixture gisuFixture;

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private ChallengerFixture challengerFixture;

    @Autowired
    private StudyGroupFixture studyGroupFixture;

    @Autowired
    private LoadStudyGroupPort loadStudyGroupPort;

}
