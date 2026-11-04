package com.umc.product.test.application.service.qa;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.GrantChallengerPointCommand;
import com.umc.product.challenger.application.port.in.query.GetChallengerPointUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.test.application.port.in.command.dto.QaSeedStep;
import com.umc.product.test.domain.roster.QaPersonas;
import com.umc.product.test.domain.roster.QaPersonas.Persona;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class QaPointsStep implements QaSeedStepRunner {

    private static final List<Persona> CURRENT_LEARNERS = List.of(
        QaPersonas.CAU_PLAN, QaPersonas.CAU_DESIGN, QaPersonas.CAU_WEB, QaPersonas.CAU_MOBILE,
        QaPersonas.CAU_WEB_INFRA, QaPersonas.CAU_MOBILE_INFRA);

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerPointUseCase getChallengerPointUseCase;
    private final ManageChallengerUseCase manageChallengerUseCase;

    @Override
    public QaSeedStep step() {
        return QaSeedStep.POINTS;
    }

    @Override
    @Transactional
    public StepTally run(QaDirectory directory) {
        List<Grant> grants = new ArrayList<>();
        CURRENT_LEARNERS.forEach(persona -> grants.add(new Grant(persona, 11)));
        QaPersonas.g10Learners().forEach(persona -> grants.add(new Grant(persona, 10)));

        StepTally tally = new StepTally();
        for (Grant grant : grants) {
            Long challengerId = getChallengerUseCase
                .findByMemberIdAndGisuId(directory.memberId(grant.persona()), directory.gisuId(grant.generation()))
                .map(ChallengerInfo::challengerId)
                .orElseThrow(() -> new IllegalStateException("QA 챌린저가 없습니다: " + grant));
            String description = "[QA] " + grant.generation() + "기 상벌점 조회 확인";
            boolean seeded = getChallengerPointUseCase.getListByChallengerId(challengerId).stream()
                .anyMatch(point -> description.equals(point.description()));
            if (seeded) {
                tally.skipped();
                continue;
            }
            for (PointType pointType : List.of(PointType.UMC_EVENT_REVIEW, PointType.STUDY_LATE)) {
                manageChallengerUseCase.grantChallengerPoint(GrantChallengerPointCommand.builder()
                    .challengerId(challengerId).pointType(pointType).description(description).build());
            }
            tally.created();
        }
        return tally;
    }

    private record Grant(Persona persona, int generation) {
    }
}
