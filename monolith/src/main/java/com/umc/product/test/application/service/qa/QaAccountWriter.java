package com.umc.product.test.application.service.qa;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.command.ManageChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.command.dto.CreateChallengerRoleCommand;
import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.test.application.port.in.command.CreateSeedMemberUseCase;
import com.umc.product.test.application.port.in.command.dto.CreateSeedMemberCommand;
import com.umc.product.test.domain.roster.QaAccountPlan.Account;
import com.umc.product.test.domain.roster.QaAccountPlan.Enrollment;
import com.umc.product.test.domain.roster.RosterRole;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class QaAccountWriter {
    private final CreateSeedMemberUseCase createSeedMemberUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;
    private final ListChallengerRoleUseCase listChallengerRoleUseCase;
    private final ManageChallengerRoleUseCase manageChallengerRoleUseCase;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean write(Account account, QaDirectory directory) {
        Long schoolId = directory.schoolId(account.schoolName());
        Long memberId = directory.findMemberId(account.email());
        boolean changed = false;
        if (memberId == null) {
            memberId = createSeedMemberUseCase.create(CreateSeedMemberCommand.of(
                account.displayName(), account.displayName(), schoolId, account.email(), null)).memberId();
            changed = true;
        }
        for (Enrollment enrollment : account.enrollments()) {
            changed |= writeEnrollment(memberId, schoolId, enrollment, directory);
        }
        return changed;
    }

    private boolean writeEnrollment(Long memberId, Long schoolId, Enrollment enrollment, QaDirectory directory) {
        Long gisuId = directory.gisuId(enrollment.generation());
        boolean changed = false;
        Long challengerId = getChallengerUseCase.findByMemberIdAndGisuId(memberId, gisuId)
            .map(ChallengerInfo::challengerId).orElse(null);
        if (challengerId == null) {
            challengerId = manageChallengerUseCase.createChallenger(CreateChallengerCommand.builder()
                .memberId(memberId).gisuId(gisuId).part(enrollment.part()).infra(enrollment.infra()).build());
            changed = true;
        }
        Set<RoleKey> existing = new HashSet<>();
        listChallengerRoleUseCase.listByMemberIdAndGisuId(memberId, gisuId).forEach(role ->
            existing.add(new RoleKey(role.roleType(), role.organizationId(), role.responsiblePart())));
        for (RosterRole role : enrollment.roles()) {
            Long organizationId = organizationId(role.type(), schoolId, enrollment.generation(), directory);
            if (existing.add(new RoleKey(role.type(), organizationId, role.responsiblePart()))) {
                manageChallengerRoleUseCase.createChallengerRole(CreateChallengerRoleCommand.builder()
                    .challengerId(challengerId).gisuId(gisuId).roleType(role.type())
                    .organizationId(organizationId).responsiblePart(role.responsiblePart()).build());
                changed = true;
            }
        }
        return changed;
    }

    private static Long organizationId(ChallengerRoleType type, Long schoolId, int generation,
        QaDirectory directory) {
        OrganizationType organizationType = type.organizationType();
        if (organizationType == OrganizationType.CENTRAL) {
            return null;
        }
        if (organizationType == OrganizationType.CHAPTER) {
            return directory.chapterIdOfSchool(generation, schoolId);
        }
        return schoolId;
    }

    private record RoleKey(ChallengerRoleType type, Long organizationId, ChallengerPart responsiblePart) {
    }
}
