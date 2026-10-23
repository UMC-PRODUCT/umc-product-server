package com.umc.product.test.application.service.qa;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerCommand;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.term.application.port.in.query.GetTermUseCase;
import com.umc.product.test.application.port.in.command.CreateSeedMemberSystemRoleUseCase;
import com.umc.product.test.application.port.in.command.CreateSeedMemberUseCase;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberCommand;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberSystemRoleCommand;
import com.umc.product.test.application.service.qa.QaSeedPersonas.Enrollment;
import com.umc.product.test.application.service.qa.QaSeedPersonas.Persona;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Transactional(propagation = Propagation.MANDATORY)
public class QaBaseSeedService {
    private final GetGisuUseCase getGisuUseCase;
    private final GetSchoolUseCase getSchoolUseCase;
    private final GetChapterUseCase getChapterUseCase;
    private final GetTermUseCase getTermUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final CreateSeedMemberUseCase createSeedMemberUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;
    private final ManageChallengerRoleUseCase manageChallengerRoleUseCase;
    private final CreateSeedMemberSystemRoleUseCase createSeedMemberSystemRoleUseCase;

    public QaSeedContext seed() {
        for (Persona persona : QaSeedPersonas.all()) {
            if (getMemberUseCase.existsByEmail(persona.email())) {
                throw new MemberDomainException(MemberErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }
        Map<Integer, Long> gisus = new LinkedHashMap<>();
        for (int generation : List.of(10, 11)) {
            List<Long> matches = getGisuUseCase.getList().stream()
                .filter(gisu -> gisu.generation() == generation).map(gisu -> gisu.gisuId()).toList();
            gisus.put(generation, unique(matches, generation + "기"));
        }
        Map<String, Long> schools = new LinkedHashMap<>();
        for (String name : List.of(QaSeedPersonas.CAU, "숙명여자대학교", "가천대학교")) {
            List<Long> matches = getSchoolUseCase.getAllSchoolNames().stream()
                .filter(school -> school.schoolName().equals(name)).map(school -> school.schoolId()).toList();
            schools.put(name, unique(matches, name));
        }
        Map<String, Long> chapters = new LinkedHashMap<>();
        requireChapter(chapters, gisus, schools, 10, QaSeedPersonas.CAU, "Xenon");
        requireChapter(chapters, gisus, schools, 11, QaSeedPersonas.CAU, "수달");
        requireChapter(chapters, gisus, schools, 11, "숙명여자대학교", "수달");
        requireChapter(chapters, gisus, schools, 11, "가천대학교", "캥거루");

        if (getTermUseCase.getRequiredTermIds().isEmpty()) {
            throw new IllegalStateException("QA 시딩에 필요한 필수 약관이 없습니다.");
        }
        Map<String, Long> members = new LinkedHashMap<>();
        Map<String, Long> challengers = new LinkedHashMap<>();
        List<Persona> personas = QaSeedPersonas.all();
        for (int index = 0; index < personas.size(); index++) {
            Persona persona = personas.get(index);
            String displayName = "QA" + (index + 1);
            Long memberId = createSeedMemberUseCase.create(CreateSeedMemberCommand.of(
                displayName, displayName, schools.get(persona.school()), persona.email(), null)).memberId();
            members.put(persona.alias(), memberId);
        }

        Long superAdminId = members.get("superadmin");
        createSeedMemberSystemRoleUseCase.create(
            new CreateSeedMemberSystemRoleCommand(superAdminId, MemberSystemRoleType.SUPER_ADMIN));
        for (Persona persona : personas) {
            Long memberId = members.get(persona.alias());
            for (Enrollment enrollment : persona.enrollments()) {
                Long gisuId = gisus.get(enrollment.generation());
                Long challengerId = manageChallengerUseCase.createChallenger(CreateChallengerCommand.builder()
                    .memberId(memberId).gisuId(gisuId).part(enrollment.part()).infra(enrollment.infra()).build());
                challengers.put(persona.alias() + ":" + enrollment.generation(), challengerId);
                if (enrollment.role() != null) {
                    Long organizationId = organizationId(enrollment, schools.get(persona.school()), gisuId);
                    manageChallengerRoleUseCase.createChallengerRole(CreateChallengerRoleCommand.builder()
                        .challengerId(challengerId).gisuId(gisuId).roleType(enrollment.role())
                        .organizationId(organizationId).responsiblePart(enrollment.responsiblePart()).build());
                }
                if (enrollment.status() != ChallengerStatus.ACTIVE) {
                    manageChallengerUseCase.updateChallenger(UpdateChallengerCommand.forStatusChange(
                        challengerId, enrollment.status(), "QA 활동 상태 검증", superAdminId));
                }
            }
        }
        return new QaSeedContext(members, challengers, gisus, schools, chapters);
    }

    private Long organizationId(Enrollment enrollment, Long schoolId, Long gisuId) {
        OrganizationType type = enrollment.role().organizationType();
        if (type == OrganizationType.CENTRAL) {
            return null;
        }
        if (type == OrganizationType.CHAPTER) {
            return getChapterUseCase.byGisuAndSchool(gisuId, schoolId).id();
        }
        return schoolId;
    }

    private void requireChapter(Map<String, Long> chapters, Map<Integer, Long> gisus, Map<String, Long> schools,
        int generation, String school, String expected) {
        List<ChapterInfo> matches = getChapterUseCase.listByGisuId(gisus.get(generation)).stream()
            .filter(chapter -> chapter.name().equals(expected)).toList();
        Long expectedId = unique(matches.stream().map(ChapterInfo::id).toList(), generation + "기 " + expected);
        ChapterInfo actual = getChapterUseCase.byGisuAndSchool(gisus.get(generation), schools.get(school));
        if (!actual.id().equals(expectedId)) {
            throw new IllegalStateException(generation + "기 " + school + "의 지부가 " + expected + "이 아닙니다.");
        }
        chapters.put(generation + ":" + expected, expectedId);
    }

    private Long unique(List<Long> values, String label) {
        if (values.size() != 1) {
            throw new IllegalStateException("QA 기준 데이터는 정확히 하나여야 합니다: " + label + " (" + values.size() + ")");
        }
        return values.getFirst();
    }
}
