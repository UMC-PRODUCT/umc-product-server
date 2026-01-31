package com.umc.product.challenger.application.service;

import com.umc.product.challenger.application.port.in.command.ManageChallengerUseCase;
import com.umc.product.challenger.application.port.in.command.dto.ChallengerDeactivationType;
import com.umc.product.challenger.application.port.in.command.dto.CreateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.DeactivateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.DeleteChallengerPointCommand;
import com.umc.product.challenger.application.port.in.command.dto.GrantChallengerPointCommand;
import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerCommand;
import com.umc.product.challenger.application.port.in.command.dto.UpdateChallengerPointCommand;
import com.umc.product.challenger.application.port.out.LoadChallengerPointPort;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPointPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.ChallengerPoint;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengerCommandService implements ManageChallengerUseCase {

    private final LoadChallengerPort loadChallengerPort;
    private final SaveChallengerPort saveChallengerPort;
    private final LoadChallengerPointPort loadChallengerPointPort;
    private final SaveChallengerPointPort saveChallengerPointPort;

    @Override
    public Long createChallenger(CreateChallengerCommand command) {
        // 동일 기수에 이미 등록된 챌린저인지 확인
        loadChallengerPort.findByMemberIdAndGisuId(command.memberId(), command.gisuId())
                .ifPresent(challenger -> {
                    throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_ALREADY_EXISTS);
                });

        Challenger challenger = new Challenger(
                command.memberId(),
                command.part(),
                command.gisuId()
        );

        Challenger savedChallenger = saveChallengerPort.save(challenger);
        return savedChallenger.getId();
    }

    @Override
    public void updateChallenger(UpdateChallengerCommand command) {
        Challenger challenger = loadChallengerPort.getById(command.challengerId());
        challenger.changePart(command.newPart());
        saveChallengerPort.save(challenger);
    }

    @Override
    public void deleteChallenger(DeleteChallengerCommand command) {
        Challenger challenger = loadChallengerPort.getById(command.challengerId());
        saveChallengerPort.delete(challenger);
    }

    @Override
    public void deactivateChallenger(DeactivateChallengerCommand command) {
        Challenger challenger = loadChallengerPort.getById(command.challengerId());
        challenger.changeStatus(resolveDeactivationStatus(command.deactivationType()));
    }

    @Override
    public void grantChallengerPoint(GrantChallengerPointCommand command) {
        Challenger challenger = loadChallengerPort.getById(command.challengerId());
        challenger.validateChallengerStatus();

        ChallengerPoint point = ChallengerPoint.create(
                challenger,
                command.pointType(),
                command.description()
        );

        challenger.addPoint(point);
        saveChallengerPort.save(challenger);
    }

    @Override
    public void updateChallengerPoint(UpdateChallengerPointCommand command) {
        ChallengerPoint point = loadChallengerPointPort.getById(command.challengerPointId());
        point.updateDescription(command.newDescription());
        saveChallengerPointPort.save(point);
    }

    @Override
    public void deleteChallengerPoint(DeleteChallengerPointCommand command) {
        ChallengerPoint point = loadChallengerPointPort.getById(command.challengerPointId());
        saveChallengerPointPort.delete(point);
    }

    private ChallengerStatus resolveDeactivationStatus(ChallengerDeactivationType deactivationType) {
        return switch (deactivationType) {
            case WITHDRAW -> ChallengerStatus.WITHDRAWN;
            case EXPEL -> ChallengerStatus.EXPELLED;
        };
    }

}
