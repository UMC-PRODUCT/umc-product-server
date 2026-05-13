package com.umc.product.organization.adapter.out.persistence.studygroup;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupInfo;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupViewScope;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupViewScope.AsPartLeader;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupViewScope.AsSchoolCore;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.domain.StudyGroupMember;
import com.umc.product.organization.domain.StudyGroupMentor;
import com.umc.product.support.TestContainersConfig;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    JpaConfig.class,
    QueryDslConfig.class,
    TestContainersConfig.class,
    StudyGroupQueryRepository.class
})
class StudyGroupQueryRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    StudyGroupQueryRepository sut;

    @Test
    void findMyStudyGroups_AsSchoolCore_scope_학교_멤버가_멤버로_등록된_그룹만_반환() {
        // given
        Long gisuId = 1L;
        Long schoolMemberA = 100L;
        Long schoolMemberB = 101L;
        Long outsider = 999L;

        StudyGroup hit1 = persistGroup("hit1", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(), Set.of(schoolMemberA));
        StudyGroup hit2 = persistGroup("hit2", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(), Set.of(schoolMemberB));
        StudyGroup miss = persistGroup("miss", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(), Set.of(outsider));
        em.flush();
        em.clear();

        List<StudyGroupViewScope> scopes = List.of(new AsSchoolCore(Set.of(schoolMemberA, schoolMemberB)));

        // when
        List<StudyGroupInfo> result = sut.findMyStudyGroups(scopes, gisuId, null, 20);

        // then
        assertThat(result).extracting(StudyGroupInfo::groupId)
            .containsExactlyInAnyOrder(hit1.getId(), hit2.getId())
            .doesNotContain(miss.getId());
    }

    @Test
    void findMyStudyGroups_AsPartLeader_scope_memberId가_멘토로_등록된_그룹만_반환() {
        // given
        Long gisuId = 1L;
        Long me = 100L;
        Long otherMentor = 200L;
        Long someMember = 300L;

        StudyGroup hit = persistGroup("hit", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(me), Set.of(someMember));
        StudyGroup miss = persistGroup("miss", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(otherMentor), Set.of(someMember));
        em.flush();
        em.clear();

        List<StudyGroupViewScope> scopes = List.of(new AsPartLeader(me));

        // when
        List<StudyGroupInfo> result = sut.findMyStudyGroups(scopes, gisuId, null, 20);

        // then
        assertThat(result).extracting(StudyGroupInfo::groupId)
            .containsExactly(hit.getId())
            .doesNotContain(miss.getId());
    }

    @Test
    void findMyStudyGroups_두_scope_OR_결합_중복은_한_번만_반환() {
        // given — schoolOnly: 회장 scope만 / mentorOnly: 파트장 scope만 / both: 둘 다 잡힘
        Long gisuId = 1L;
        Long me = 100L;
        Long schoolMember = 101L;
        Long otherMentor = 200L;
        Long outsider = 999L;

        StudyGroup schoolOnly = persistGroup("schoolOnly", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(otherMentor), Set.of(schoolMember));
        StudyGroup mentorOnly = persistGroup("mentorOnly", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(me), Set.of(outsider));
        StudyGroup both = persistGroup("both", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(me), Set.of(schoolMember));
        em.flush();
        em.clear();

        List<StudyGroupViewScope> scopes = List.of(
            new AsSchoolCore(Set.of(schoolMember)),
            new AsPartLeader(me)
        );

        // when
        List<StudyGroupInfo> result = sut.findMyStudyGroups(scopes, gisuId, null, 20);

        // then
        assertThat(result).extracting(StudyGroupInfo::groupId)
            .containsExactlyInAnyOrder(schoolOnly.getId(), mentorOnly.getId(), both.getId());
    }

    @Test
    void findMyStudyGroups_다른_기수_그룹은_제외() {
        // given
        Long activeGisu = 1L;
        Long otherGisu = 2L;
        Long me = 100L;
        Long someMember = 200L;

        StudyGroup activeGroup = persistGroup("active", activeGisu, ChallengerPart.SPRINGBOOT,
            Set.of(me), Set.of(someMember));
        StudyGroup otherGroup = persistGroup("other", otherGisu, ChallengerPart.SPRINGBOOT,
            Set.of(me), Set.of(someMember));
        em.flush();
        em.clear();

        List<StudyGroupViewScope> scopes = List.of(new AsPartLeader(me));

        // when
        List<StudyGroupInfo> result = sut.findMyStudyGroups(scopes, activeGisu, null, 20);

        // then
        assertThat(result).extracting(StudyGroupInfo::groupId)
            .containsExactly(activeGroup.getId())
            .doesNotContain(otherGroup.getId());
    }

    @Test
    void findMyStudyGroups_커서_페이지네이션_id_DESC로_lt_cursor만_반환() {
        // given — id가 더 작은(=오래된) 그룹만 다음 페이지에 잡혀야 함
        Long gisuId = 1L;
        Long me = 100L;
        Long someMember = 200L;

        StudyGroup oldest = persistGroup("oldest", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(me), Set.of(someMember));
        StudyGroup middle = persistGroup("middle", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(me), Set.of(someMember));
        StudyGroup newest = persistGroup("newest", gisuId, ChallengerPart.SPRINGBOOT,
            Set.of(me), Set.of(someMember));
        em.flush();
        em.clear();

        List<StudyGroupViewScope> scopes = List.of(new AsPartLeader(me));

        // when — newest 다음 페이지
        List<StudyGroupInfo> result = sut.findMyStudyGroups(scopes, gisuId, newest.getId(), 20);

        // then
        assertThat(result).extracting(StudyGroupInfo::groupId)
            .containsExactly(middle.getId(), oldest.getId())
            .doesNotContain(newest.getId());
    }

    @Test
    void findMyStudyGroups_모든_scope의_predicate가_null이면_빈_결과() {
        // given — buildScopePredicate 가 null 반환하는 경로 (AsSchoolCore 의 schoolMemberIds 가 비어있음).
        //         이런 입력이 들어와도 풀스캔 없이 빈 리스트가 되어야 한다.
        Long gisuId = 1L;
        persistGroup("g1", gisuId, ChallengerPart.SPRINGBOOT, Set.of(), Set.of(100L));
        em.flush();
        em.clear();

        List<StudyGroupViewScope> scopes = List.of(new AsSchoolCore(Set.of()));

        // when
        List<StudyGroupInfo> result = sut.findMyStudyGroups(scopes, gisuId, null, 20);

        // then
        assertThat(result).isEmpty();
    }

    // ========== Helper Methods ==========

    private StudyGroup persistGroup(
        String name, Long gisuId, ChallengerPart part,
        Set<Long> mentorIds, Set<Long> memberIds
    ) {
        StudyGroup group = newStudyGroup(name, gisuId, part);
        em.persist(group);
        memberIds.forEach(memberId -> persistMember(group, memberId));
        mentorIds.forEach(mentorId -> persistMentor(group, mentorId));
        return group;
    }

    private StudyGroup newStudyGroup(String name, Long gisuId, ChallengerPart part) {
        StudyGroup group;
        try {
            var constructor = StudyGroup.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            group = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(group, "name", name);
        ReflectionTestUtils.setField(group, "gisuId", gisuId);
        ReflectionTestUtils.setField(group, "part", part);
        return group;
    }

    private void persistMember(StudyGroup group, Long memberId) {
        StudyGroupMember entity;
        try {
            var constructor = StudyGroupMember.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            entity = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(entity, "studyGroup", group);
        ReflectionTestUtils.setField(entity, "memberId", memberId);
        ReflectionTestUtils.setField(entity, "isLeader", false);
        em.persist(entity);
    }

    private void persistMentor(StudyGroup group, Long memberId) {
        StudyGroupMentor entity;
        try {
            var constructor = StudyGroupMentor.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            entity = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(entity, "studyGroup", group);
        ReflectionTestUtils.setField(entity, "memberId", memberId);
        em.persist(entity);
    }
}
