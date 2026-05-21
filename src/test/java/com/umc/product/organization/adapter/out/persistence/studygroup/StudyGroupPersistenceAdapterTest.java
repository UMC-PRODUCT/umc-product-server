package com.umc.product.organization.adapter.out.persistence.studygroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.domain.StudyGroupMember;
import com.umc.product.organization.domain.StudyGroupMentor;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import com.umc.product.support.TestContainersConfig;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    JpaConfig.class,
    QueryDslConfig.class,
    TestContainersConfig.class,
    StudyGroupQueryRepository.class,
    StudyGroupPersistenceAdapter.class
})
class StudyGroupPersistenceAdapterTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    StudyGroupPersistenceAdapter sut;

    @Test
    void save_도메인_정상경로로_생성한_그룹은_자식까지_cascade로_보존된다() {
        // given — 도메인 팩토리/도메인 메서드만 사용. @OneToMany cascade 가 빠지면 자식이 안 따라가 reload 시 빈 컬렉션이 됨.
        StudyGroup group = StudyGroup.create("cascade-test", 1L, ChallengerPart.SPRINGBOOT);
        group.addMembers(Set.of(100L, 101L));
        group.assignMentors(Set.of(200L));

        // when
        StudyGroup saved = sut.save(group);
        em.flush();
        em.clear();

        // then — DB 왕복 후 자식 컬렉션이 그대로 살아있는지
        StudyGroup reloaded = em.find(StudyGroup.class, saved.getId());
        assertThat(reloaded.getMembers())
            .extracting(StudyGroupMember::getMemberId)
            .containsExactlyInAnyOrder(100L, 101L);
        assertThat(reloaded.getMentors())
            .extracting(StudyGroupMentor::getMemberId)
            .containsExactly(200L);
    }

    @Test
    void getEntityById_존재하지_않으면_OrganizationDomainException_STUDY_GROUP_NOT_FOUND() {
        // when & then
        assertThatThrownBy(() -> sut.getEntityById(99999L))
            .isInstanceOf(OrganizationDomainException.class)
            .extracting("baseCode")
            .isEqualTo(OrganizationErrorCode.STUDY_GROUP_NOT_FOUND);
    }

    @Test
    void getByName_존재하지_않으면_OrganizationDomainException_STUDY_GROUP_NOT_FOUND() {
        // when & then
        assertThatThrownBy(() -> sut.getByName("존재하지_않는_그룹"))
            .isInstanceOf(OrganizationDomainException.class)
            .extracting("baseCode")
            .isEqualTo(OrganizationErrorCode.STUDY_GROUP_NOT_FOUND);
    }
}
