package com.umc.product.support.fixture;

import java.time.Instant;
import java.util.Collection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.form.domain.Form;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationFormPort;
import com.umc.product.project.domain.ProjectApplicationForm;

import jakarta.persistence.EntityManager;

@Component
@Transactional
public class ProjectDraftFixture {

    private final GisuFixture gisuFixture;
    private final ChapterFixture chapterFixture;
    private final SchoolFixture schoolFixture;
    private final ChallengerFixture challengerFixture;
    private final SaveMemberPort saveMemberPort;
    private final LoadProjectPort loadProjectPort;
    private final SaveProjectApplicationFormPort saveProjectApplicationFormPort;
    private final EntityManager entityManager;

    public ProjectDraftFixture(GisuFixture gisuFixture, ChapterFixture chapterFixture,
                               SchoolFixture schoolFixture, ChallengerFixture challengerFixture,
                               SaveMemberPort saveMemberPort, LoadProjectPort loadProjectPort,
                               SaveProjectApplicationFormPort saveProjectApplicationFormPort,
                               EntityManager entityManager) {
        this.gisuFixture = gisuFixture;
        this.chapterFixture = chapterFixture;
        this.schoolFixture = schoolFixture;
        this.challengerFixture = challengerFixture;
        this.saveMemberPort = saveMemberPort;
        this.loadProjectPort = loadProjectPort;
        this.saveProjectApplicationFormPort = saveProjectApplicationFormPort;
        this.entityManager = entityManager;
    }

    public Context 기획자(String name, Long generation) {
        var gisu = gisuFixture.비활성_기수(generation);
        var chapter = chapterFixture.지부(gisu, name + "지부");
        var school = schoolFixture.지부에_소속된_학교(name + "학교", chapter);
        Member member = saveMemberPort.save(Member.create(name, name, name + "@test.com", school.getId(), null));
        challengerFixture.챌린저(member.getId(), ChallengerPart.PLAN, gisu.getId());
        return new Context(member.getId(), gisu.getId(), school.getId());
    }

    public Context 같은_기수의_다른_기획자(Context context, String name) {
        Member member = saveMemberPort.save(Member.create(name, name, name + "@test.com", context.schoolId(), null));
        challengerFixture.챌린저(member.getId(), ChallengerPart.PLAN, context.gisuId());
        return new Context(member.getId(), context.gisuId(), context.schoolId());
    }

    public void 생성시각_통일(Collection<Long> ids) {
        Instant time = Instant.parse("2026-01-01T00:00:00Z");
        for (Long id : ids) {
            entityManager.createNativeQuery("UPDATE project SET created_at = :time WHERE id = :id")
                .setParameter("time", time).setParameter("id", id).executeUpdate();
        }
    }

    public void 지원폼(Long projectId, Long memberId) {
        Form form = Form.createDraft("프로젝트 지원서", memberId);
        entityManager.persist(form);
        saveProjectApplicationFormPort.save(ProjectApplicationForm.create(loadProjectPort.getById(projectId), form.getId()));
    }

    public record Context(Long memberId, Long gisuId, Long schoolId) {
    }
}
