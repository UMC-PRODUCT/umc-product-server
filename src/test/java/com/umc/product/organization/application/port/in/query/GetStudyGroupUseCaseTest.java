package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.out.command.SaveSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadStudyGroupPort;
import com.umc.product.support.UseCaseTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.MemberFixture;
import com.umc.product.support.fixture.StudyGroupFixture;
import org.springframework.beans.factory.annotation.Autowired;

class GetStudyGroupUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetStudyGroupUseCase getStudyGroupUseCase;

    @Autowired
    private SaveSchoolPort saveSchoolPort;

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
