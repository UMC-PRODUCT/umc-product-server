package com.umc.product.organization.application.port.service.command;

import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateGisuCommand;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.query.LoadChapterPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GisuService implements ManageGisuUseCase {

    private final LoadGisuPort loadGisuPort;
    private final ManageGisuPort manageGisuPort;
    private final LoadChapterPort loadChapterPort;

    @Override
    public Long create(CreateGisuCommand command) {
        validateGenerationNotDuplicated(command);

        Gisu gisu = Gisu.create(command.generation(), command.startAt(), command.endAt(), false);

        return manageGisuPort.save(gisu).getId();
    }


    @Override
    public void deleteGisu(Long gisuId) {
        Gisu gisu = loadGisuPort.findById(gisuId);
        if (loadChapterPort.existsByGisuId(gisuId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.GISU_HAS_ASSOCIATED_CHAPTERS);
        }
        manageGisuPort.delete(gisu);
    }

    @Override
    public void updateActiveGisu(Long gisuId) {

        Optional<Gisu> oldActiveGisuOptional = loadGisuPort.findActiveGisuWithLock();

        // 활성화하려는 기수가 이미 활성 상태인 경우, 추가 작업 없이 종료
        if (oldActiveGisuOptional.isPresent() && oldActiveGisuOptional.get().getId().equals(gisuId)) {
            return;
        }

        // 기존 활성 기수가 있다면 비활성화
        oldActiveGisuOptional.ifPresent(Gisu::inactive);

        // 새로운 기수를 활성화
        Gisu newGisu = loadGisuPort.findById(gisuId);
        newGisu.active();
    }

    private void validateGenerationNotDuplicated(CreateGisuCommand command) {
        if (loadGisuPort.existsByGeneration(command.generation())) {
            throw new OrganizationDomainException(OrganizationErrorCode.GISU_ALREADY_EXISTS);
        }
    }
}
