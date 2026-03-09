package com.umc.product.organization.application.port.service.command;

import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.domain.OrganizationDomainException;
import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.exception.OrganizationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GisuService implements ManageGisuUseCase {

    private final LoadGisuPort loadGisuPort;
    private final ManageGisuPort manageGisuPort;

    @Override
    public Long create(CreateGisuCommand command) {
        validateGenerationNotDuplicated(command);

        Gisu gisu = Gisu.create(command.generation(), command.startAt(), command.endAt(), false);

        return manageGisuPort.save(gisu).getId();
    }


    @Override
    public void deleteGisu(Long gisuId) {
        Gisu gisu = loadGisuPort.findById(gisuId);
        manageGisuPort.delete(gisu);
    }

    @Override
    public void updateActiveGisu(Long gisuId) {

        /**
         * active 상태인 기존 기수가 없다면 findActiveGisuWithLock()는 Optional.empty()를 반환하므로,
         * ifPresent()는 아무 작업도 수행하지 않고 새로운 기수만 active 상태로 변경됩니다.
         */
        loadGisuPort.findActiveGisuWithLock().ifPresent(Gisu::inactive);

        Gisu newGisu = loadGisuPort.findById(gisuId);
        newGisu.active();
    }

    private void validateGenerationNotDuplicated(CreateGisuCommand command) {
        if (loadGisuPort.existsByGeneration(command.generation())) {
            throw new OrganizationDomainException(OrganizationErrorCode.GISU_ALREADY_EXISTS);
        }
    }
}
