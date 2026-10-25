package com.umc.product.project.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.SearchProjectQuery;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.PersistenceAdapterTest;

@PersistenceAdapterTest
@Import(ProjectQueryRepository.class)
class ProjectMatchingListOrderTest {
    private static final Instant NOW = Instant.parse("2026-11-13T03:00:00Z");
    @Autowired TestEntityManager em;
    @Autowired ProjectQueryRepository sut;

    @Test
    void 초안_생성순이나_이름이_아닌_PM_제출순으로_조회한다() {
        // given
        Project createdFirst = project("A 먼저 생성", 40, 1L);
        Project submittedFirst = project("Z 먼저 제출", 10, 1L);
        flush();

        // when & then
        assertOrder(query(NOW), submittedFirst, createdFirst);
        // 일반 목록은 기존 이름 정렬을 유지한다.
        assertOrder(SearchProjectQuery.forChallenger(1L, null, null, null, null, null,
            PageRequest.of(0, 20, Sort.by("name"))), createdFirst, submittedFirst);
    }

    @Test
    void 디자인과_개발_TO를_구분하고_개발파트가_모두_차야_완료로_판단한다() {
        // given
        Project designFull = project("디자인 완료", 10, 1L);
        quota(designFull, ChallengerPart.DESIGN, 1);
        member(designFull, ChallengerPart.DESIGN, ProjectMemberStatus.ACTIVE);
        quota(designFull, ChallengerPart.WEB_PRODUCT_ENGINEER, 1);
        member(designFull, ChallengerPart.WEB_PRODUCT_ENGINEER, ProjectMemberStatus.ACTIVE);
        quota(designFull, ChallengerPart.MOBILE_PRODUCT_ENGINEER, 1);
        quota(designFull, ChallengerPart.SPRINGBOOT, 1);
        member(designFull, ChallengerPart.SPRINGBOOT, ProjectMemberStatus.ACTIVE);
        Project developersFull = project("개발 완료", 20, 1L);
        quota(developersFull, ChallengerPart.DESIGN, 1);
        quota(developersFull, ChallengerPart.WEB_PRODUCT_ENGINEER, 1);
        quota(developersFull, ChallengerPart.SPRINGBOOT, 1);
        member(developersFull, ChallengerPart.WEB_PRODUCT_ENGINEER, ProjectMemberStatus.ACTIVE);
        member(developersFull, ChallengerPart.SPRINGBOOT, ProjectMemberStatus.ACTIVE);
        Project recruiting = project("모집중", 30, 1L);
        quota(recruiting, ChallengerPart.DESIGN, 1);
        quota(recruiting, ChallengerPart.SPRINGBOOT, 1);
        round(1L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -60, 60, 120, 180);
        round(1L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -60, 60, 120, 180);
        flush();

        // when & then
        assertOrder(queryForPart(ChallengerPart.DESIGN), developersFull, recruiting, designFull);
        assertOrder(queryForPart(ChallengerPart.SPRINGBOOT), designFull, recruiting, developersFull);
    }

    @Test
    void TO가_없거나_미발표_탈퇴_해제_멤버만_있으면_모집완료로_보지_않는다() {
        // given
        Project noQuota = project("TO 없음", 10, 1L);
        Project pending = project("미발표", 20, 1L);
        Project withdrawn = project("탈퇴", 30, 1L);
        Project dismissed = project("해제", 40, 1L);
        Project full = project("완료", 0, 1L);
        for (Project project : List.of(pending, withdrawn, dismissed, full)) {
            quota(project, ChallengerPart.DESIGN, 1);
        }
        member(pending, ChallengerPart.DESIGN, ProjectMemberStatus.PENDING_PUBLICATION);
        member(withdrawn, ChallengerPart.DESIGN, ProjectMemberStatus.WITHDRAWN);
        member(dismissed, ChallengerPart.DESIGN, ProjectMemberStatus.DISMISSED);
        member(full, ChallengerPart.DESIGN, ProjectMemberStatus.ACTIVE);
        round(1L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -60, 60, 120, 180);
        flush();

        // when & then
        assertOrder(query(NOW), noQuota, pending, withdrawn, dismissed, full);
    }

    @ParameterizedTest
    @CsvSource({"-101,false", "-100,true", "0,true", "100,true", "101,false"})
    void 첫_차수부터_최종_발표까지_차수_사이에도_하단배치를_유지한다(long nowOffset, boolean inPeriod) {
        // given
        Project full = fullProject("완료", 10, 1L);
        Project recruiting = project("모집중", 20, 1L);
        round(1L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60, -40);
        round(1L, MatchingType.PLAN_DESIGN, MatchingPhase.SECOND, 40, 60, 80, 100);
        flush();

        // when & then
        assertOrder(query(NOW.plusSeconds(nowOffset * 60)),
            inPeriod ? new Project[]{recruiting, full} : new Project[]{full, recruiting});
    }

    @Test
    void 발표시각이_없는_차수는_결정마감을_기준으로_기간을_종료한다() {
        // given
        Project full = fullProject("완료", 10, 1L);
        Project recruiting = project("모집중", 20, 1L);
        em.persist(ProjectMatchingRound.create("기존 차수", null, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST,
            1L, NOW.minusSeconds(60), NOW.minusSeconds(30), NOW, null));
        flush();

        // when & then
        assertOrder(query(NOW), recruiting, full);
        assertOrder(query(NOW.plusSeconds(1)), full, recruiting);
    }

    @Test
    void 다른_지부와_매칭유형의_일정으로_하단에_배치하지_않는다() {
        // given
        Project first = fullProject("먼저 제출", 10, 1L);
        Project second = project("나중 제출", 20, 1L);
        round(2L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -60, 60, 120, 180);
        round(1L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -60, 60, 120, 180);
        flush();

        // when & then
        assertOrder(query(NOW), first, second);
    }

    @Test
    void 동일_제출시각의_프로젝트도_페이지_사이에_중복이나_누락이_없다() {
        // given
        Project full = fullProject("완료", 0, 1L);
        List<Project> open = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            open.add(project("동률 " + i, 10, 1L));
        }
        round(1L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -60, 60, 120, 180);
        flush();
        List<Long> ids = new ArrayList<>();

        // when
        for (int page = 0; page < 3; page++) {
            Page<Project> result = sut.search(query(NOW, PageRequest.of(page, 2)));
            assertThat(result.getTotalElements()).isEqualTo(6);
            ids.addAll(result.getContent().stream().map(Project::getId).toList());
        }

        // then
        List<Long> expected = new ArrayList<>(open.stream().map(Project::getId).toList());
        expected.add(full.getId());
        assertThat(ids).containsExactlyElementsOf(expected).doesNotHaveDuplicates();
    }

    @Test
    void 저장된_초안을_제출하면_시각이_DB에_기록되고_승인해도_유지된다() {
        // given
        Project draft = Project.createDraft(1L, 1L, 100L, 1L, 100L);
        draft.updateBasicInfo("제출 전", null, null, null, null);
        em.persist(draft);
        flush();
        Instant before = Instant.now();

        // when
        Project loaded = em.find(Project.class, draft.getId());
        assertThat(loaded.getSubmittedAt()).isNull();
        loaded.submit();
        flush();
        Instant submitted = em.find(Project.class, draft.getId()).getSubmittedAt();
        em.find(Project.class, draft.getId()).publish();
        flush();

        // then
        assertThat(submitted).isBetween(before.minusSeconds(1), Instant.now());
        assertThat(em.find(Project.class, draft.getId()).getSubmittedAt()).isEqualTo(submitted);
    }

    @Test
    void 기존_프로젝트의_제출시각을_임의로_채우지_않는다() {
        // given
        Project legacy = project("이전 데이터", 0, 1L);
        ReflectionTestUtils.setField(legacy, "submittedAt", null);
        Project submitted = project("새 제출", 10, 1L);
        flush();

        // when & then
        assertOrder(query(NOW), legacy, submitted);
        assertThat(em.find(Project.class, legacy.getId()).getSubmittedAt()).isNull();
    }

    @Test
    void 파트필터가_없으면_진행중인_매칭유형의_TO만_자동으로_판단한다() {
        // given
        Project designFull = fullProject("디자인 완료", 10, 1L);
        quota(designFull, ChallengerPart.SPRINGBOOT, 1);
        Project developersFull = project("개발 완료", 20, 1L);
        quota(developersFull, ChallengerPart.DESIGN, 1);
        quota(developersFull, ChallengerPart.SPRINGBOOT, 1);
        member(developersFull, ChallengerPart.SPRINGBOOT, ProjectMemberStatus.ACTIVE);
        round(1L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -100, -80, -60, -40);
        round(1L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, 40, 60, 80, 100);
        flush();

        // when & then
        assertOrder(query(NOW.minusSeconds(90 * 60)), developersFull, designFull);
        assertOrder(query(NOW.plusSeconds(50 * 60)), designFull, developersFull);
    }

    @Test
    void 디자인과_개발이_동시에_진행되면_둘_다_찬_프로젝트만_전체목록의_하단으로_보낸다() {
        // given
        Project allFull = fullProject("전체 완료", 0, 1L);
        quota(allFull, ChallengerPart.SPRINGBOOT, 1);
        member(allFull, ChallengerPart.SPRINGBOOT, ProjectMemberStatus.ACTIVE);
        Project designOnly = fullProject("개발 모집중", 10, 1L);
        quota(designOnly, ChallengerPart.SPRINGBOOT, 1);
        Project recruiting = project("모집중", 20, 1L);
        quota(recruiting, ChallengerPart.DESIGN, 1);
        round(1L, MatchingType.PLAN_DESIGN, MatchingPhase.FIRST, -60, 60, 120, 180);
        round(1L, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST, -60, 60, 120, 180);
        flush();

        // when & then
        assertOrder(query(NOW), designOnly, recruiting, allFull);
    }

    private SearchProjectQuery query(Instant now) {
        return query(now, PageRequest.of(0, 20));
    }

    private SearchProjectQuery query(Instant now, Pageable pageable) {
        return SearchProjectQuery.builder().gisuId(1L).statuses(List.of(ProjectStatus.IN_PROGRESS))
            .matchingListOrder(true).matchingOrderAt(now).pageable(pageable).build();
    }

    private SearchProjectQuery queryForPart(ChallengerPart part) {
        return SearchProjectQuery.builder().gisuId(1L).statuses(List.of(ProjectStatus.IN_PROGRESS))
            .parts(List.of(part)).matchingListOrder(true).matchingOrderAt(NOW).pageable(PageRequest.of(0, 20)).build();
    }

    private Project project(String name, long submittedSeconds, Long chapterId) {
        Project project = Project.createDraft(1L, chapterId, 100L, 1L, 100L);
        project.updateBasicInfo(name, null, null, null, null);
        project.submit();
        project.publish();
        ReflectionTestUtils.setField(project, "submittedAt", NOW.minusSeconds(1000).plusSeconds(submittedSeconds));
        em.persist(project);
        return project;
    }

    private Project fullProject(String name, long submittedSeconds, Long chapterId) {
        Project project = project(name, submittedSeconds, chapterId);
        quota(project, ChallengerPart.DESIGN, 1);
        member(project, ChallengerPart.DESIGN, ProjectMemberStatus.ACTIVE);
        return project;
    }

    private void quota(Project project, ChallengerPart part, long count) {
        em.persist(ProjectPartQuota.create(project, part, count, 100L));
    }

    private void member(Project project, ChallengerPart part, ProjectMemberStatus status) {
        ProjectMember member = ProjectMember.create(project, 200L + project.getId() * 10 + part.ordinal(), part, 100L);
        ReflectionTestUtils.setField(member, "status", status);
        em.persist(member);
    }

    private void round(Long chapterId, MatchingType type, MatchingPhase phase,
        long startsIn, long endsIn, long deadlineIn, long publicationIn) {
        em.persist(ProjectMatchingRound.create("매칭 차수", null, type, phase, chapterId,
            NOW.plusSeconds(startsIn * 60), NOW.plusSeconds(endsIn * 60), NOW.plusSeconds(deadlineIn * 60),
            NOW.plusSeconds(publicationIn * 60)));
    }

    private void flush() {
        em.flush();
        em.clear();
    }

    private void assertOrder(SearchProjectQuery query, Project... expected) {
        assertThat(sut.search(query).getContent()).extracting(Project::getId)
            .containsExactly(java.util.Arrays.stream(expected).map(Project::getId).toArray(Long[]::new));
    }
}
