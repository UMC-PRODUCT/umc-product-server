package com.umc.product.support.fixture;

import java.time.Instant;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;

@Component
@Transactional
public class ProjectQuotaManagementFixture {
    private final ProjectManualMatchingFixture projects;
    private final LoadProjectPort loadProjectPort;
    private final SaveProjectPort saveProjectPort;
    private final SaveProjectPartQuotaPort saveProjectPartQuotaPort;
    private final SaveProjectMemberPort saveProjectMemberPort;
    private final SaveProjectMatchingRoundPort saveProjectMatchingRoundPort;

    public ProjectQuotaManagementFixture(ProjectManualMatchingFixture projects, LoadProjectPort loadProjectPort,
        SaveProjectPort saveProjectPort, SaveProjectPartQuotaPort saveProjectPartQuotaPort,
        SaveProjectMemberPort saveProjectMemberPort, SaveProjectMatchingRoundPort saveProjectMatchingRoundPort) {
        this.projects = projects;
        this.loadProjectPort = loadProjectPort;
        this.saveProjectPort = saveProjectPort;
        this.saveProjectPartQuotaPort = saveProjectPartQuotaPort;
        this.saveProjectMemberPort = saveProjectMemberPort;
        this.saveProjectMatchingRoundPort = saveProjectMatchingRoundPort;
    }

    public Scenario 기본() {
        Scenario scenario = projects.준비();
        projects.회원(scenario, "디자인 추가", ChallengerPart.DESIGN);
        projects.회원(scenario, "웹 하나", ChallengerPart.WEB_PRODUCT_ENGINEER);
        projects.회원(scenario, "웹 둘", ChallengerPart.WEB_PRODUCT_ENGINEER);
        projects.회원(scenario, "모바일 하나", ChallengerPart.MOBILE_PRODUCT_ENGINEER);
        return scenario;
    }

    public Long 프로젝트(Scenario scenario, String name) {
        Project project = Project.createDraft(scenario.gisuId(), scenario.chapterId(), scenario.ownerId(),
            scenario.schoolId(), scenario.ownerId());
        project.updateBasicInfo(name, null, null, null, null);
        project.submit();
        project.publish();
        return saveProjectPort.save(project).getId();
    }

    public void 정원(Long projectId, ChallengerPart part, long quota) {
        saveProjectPartQuotaPort.deleteByProjectIdAndPartIn(projectId, Set.of(part));
        if (quota > 0) {
            saveProjectPartQuotaPort.save(ProjectPartQuota.create(loadProjectPort.getById(projectId), part, quota, 1L));
        }
    }

    public void 배정(Scenario scenario, Long memberId, ChallengerPart part, boolean pending) {
        ProjectMember member = ProjectMember.create(loadProjectPort.getById(scenario.projectId()), memberId,
            part, scenario.staffId());
        if (pending) {
            ReflectionTestUtils.setField(member, "status", ProjectMemberStatus.PENDING_PUBLICATION);
        }
        saveProjectMemberPort.save(member);
    }

    public Long 해제예약(Scenario scenario, Instant now) {
        ProjectMatchingRound round = saveProjectMatchingRoundPort.save(ProjectMatchingRound.create(
            "TO 조정", null, MatchingType.PLAN_DESIGN, MatchingPhase.RANDOM_MATCHING, scenario.chapterId(),
            now.minusSeconds(3600), now.plusSeconds(3600), now.plusSeconds(3600), now.plusSeconds(7200)));
        round.executeAutoDecision(scenario.staffId());
        saveProjectMatchingRoundPort.save(round);
        ProjectMember member = ProjectMember.create(loadProjectPort.getById(scenario.projectId()),
            scenario.candidateId(), ChallengerPart.DESIGN, scenario.staffId());
        member.scheduleRemoval(round, scenario.staffId(), "팀 이동");
        saveProjectMemberPort.save(member);
        return round.getId();
    }

    public void 차수(Scenario scenario, MatchingType type, MatchingPhase phase,
        Instant startsAt, Instant endsAt, Instant deadline) {
        saveProjectMatchingRoundPort.save(ProjectMatchingRound.create("TO 기간 검증", null, type, phase,
            scenario.chapterId(), startsAt, endsAt, deadline, deadline.plusSeconds(3600)));
    }
}
