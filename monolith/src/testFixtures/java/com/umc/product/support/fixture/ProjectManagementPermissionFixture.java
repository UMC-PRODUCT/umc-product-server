package com.umc.product.support.fixture;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.authorization.application.port.out.SaveChallengerRolePort;
import com.umc.product.authorization.domain.ChallengerRole;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.project.application.port.out.SaveProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.support.fixture.ProjectManualMatchingFixture.Scenario;

import jakarta.persistence.EntityManager;

@Component
@Transactional
public class ProjectManagementPermissionFixture {
    private final ProjectManualMatchingFixture projectFixture;
    private final GetChallengerUseCase getChallengerUseCase;
    private final SaveChallengerRolePort saveRolePort;
    private final SaveProjectPort saveProjectPort;
    private final EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    private final EntityManager entityManager;

    public ProjectManagementPermissionFixture(ProjectManualMatchingFixture projectFixture,
        GetChallengerUseCase getChallengerUseCase, SaveChallengerRolePort saveRolePort,
        SaveProjectPort saveProjectPort, EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase,
        EntityManager entityManager) {
        this.projectFixture = projectFixture;
        this.getChallengerUseCase = getChallengerUseCase;
        this.saveRolePort = saveRolePort;
        this.saveProjectPort = saveProjectPort;
        this.evictAuthoritySnapshotCacheUseCase = evictAuthoritySnapshotCacheUseCase;
        this.entityManager = entityManager;
    }

    public Long 역할_회원(Scenario scenario, ChallengerRoleType role, ChallengerPart part) {
        Long memberId = projectFixture.회원(scenario, "권한테스트", part);
        역할_추가(scenario, memberId, role);
        return memberId;
    }

    public void 역할_추가(Scenario scenario, Long memberId, ChallengerRoleType role) {
        Long organizationId = switch (role.organizationType()) {
            case CENTRAL -> null;
            case CHAPTER -> scenario.chapterId();
            case SCHOOL -> scenario.schoolId();
            default -> throw new IllegalArgumentException("프로젝트 권한 테스트에서 사용하지 않는 역할입니다.");
        };
        Long challengerId = getChallengerUseCase.getByMemberIdAndGisuId(memberId, scenario.gisuId()).challengerId();
        saveRolePort.save(ChallengerRole.create(challengerId, role, organizationId,
            ChallengerPart.DESIGN, scenario.gisuId()));
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
    }

    public void 최고관리자(Long memberId) {
        entityManager.persist(MemberSystemRole.create(memberId, MemberSystemRoleType.SUPER_ADMIN));
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(memberId);
    }

    public Long 프로젝트(Scenario scenario, Long ownerId, ProjectStatus status) {
        Project project = Project.createDraft(scenario.gisuId(), scenario.chapterId(), ownerId,
            scenario.schoolId(), ownerId);
        project.updateBasicInfo("권한 검증 " + status.name(), null, null, null, null);
        if (status != ProjectStatus.DRAFT) {
            project.submit();
        }
        if (status == ProjectStatus.IN_PROGRESS || status == ProjectStatus.COMPLETED || status == ProjectStatus.ABORTED) {
            project.publish();
        }
        if (status == ProjectStatus.COMPLETED) {
            project.complete(scenario.staffId());
        }
        if (status == ProjectStatus.ABORTED) {
            project.abort("중단", scenario.staffId());
        }
        return saveProjectPort.save(project).getId();
    }

    public Map<ProjectStatus, Long> 모든_상태(Scenario scenario) {
        Map<ProjectStatus, Long> ids = new EnumMap<>(ProjectStatus.class);
        for (ProjectStatus status : ProjectStatus.values()) {
            ids.put(status, status == ProjectStatus.IN_PROGRESS ? scenario.projectId()
                : 프로젝트(scenario, scenario.ownerId(), status));
        }
        return ids;
    }
}
