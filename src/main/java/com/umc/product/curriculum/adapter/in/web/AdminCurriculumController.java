package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.request.ManageCurriculumRequest;
import com.umc.product.curriculum.adapter.in.web.dto.response.AdminCurriculumResponse;
import com.umc.product.curriculum.application.port.in.command.ManageCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.query.GetAdminCurriculumUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/curriculums")
@RequiredArgsConstructor
public class AdminCurriculumController implements AdminCurriculumControllerApi {

    private final GetAdminCurriculumUseCase getAdminCurriculumUseCase;
    private final ManageCurriculumUseCase manageCurriculumUseCase;

    @Override
    @GetMapping
    public AdminCurriculumResponse getCurriculum(
            @RequestParam ChallengerPart part
    ) {
        return AdminCurriculumResponse.from(getAdminCurriculumUseCase.getByActiveGisuAndPart(part));
    }

    @Override
    @PutMapping
    public void manageCurriculum(
            @Valid @RequestBody ManageCurriculumRequest request
    ) {
        manageCurriculumUseCase.manage(request.toCommand());
    }
}
