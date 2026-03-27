package com.umc.product.curriculum.adapter.in.web.v1;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.curriculum.adapter.in.web.v1.dto.request.ManageCurriculumRequest;
import com.umc.product.curriculum.adapter.in.web.v1.swagger.AdminCurriculumControllerApi;
import com.umc.product.curriculum.application.port.in.command.ManageCurriculumUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curriculums")
@RequiredArgsConstructor
public class AdminCurriculumController implements AdminCurriculumControllerApi {

    private final ManageCurriculumUseCase manageCurriculumUseCase;

    @CheckAccess(
        resourceType = ResourceType.ORIGINAL_WORKBOOK,
        permission = PermissionType.MANAGE,
        message = "커리큘럼은 중앙운영사무국 교육국 소속 파트장만 관리할 수 있습니다."
    )
    @Override
    @PutMapping
    public void manageCurriculum(
        @Valid @RequestBody ManageCurriculumRequest request
    ) {
        manageCurriculumUseCase.manage(request.toCommand());
    }
}
