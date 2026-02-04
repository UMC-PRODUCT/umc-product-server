package com.umc.product.curriculum.adapter.in.web;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.in.web.dto.request.ManageCurriculumRequest;
import com.umc.product.curriculum.adapter.in.web.dto.response.AdminCurriculumResponse;
import com.umc.product.curriculum.application.port.in.command.ManageCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.query.GetAdminCurriculumUseCase;
import com.umc.product.global.security.annotation.Public;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/curriculums")
@RequiredArgsConstructor
public class AdminCurriculumController implements AdminCurriculumControllerApi {

    private final GetAdminCurriculumUseCase getAdminCurriculumUseCase;
    private final ManageCurriculumUseCase manageCurriculumUseCase;

    @Override
    @GetMapping
    public AdminCurriculumResponse getCurriculum(
            @RequestParam ChallengerPart part
    ) {
        // TODO: user의 권한에 따라 막히게 구현 필요
        return AdminCurriculumResponse.from(getAdminCurriculumUseCase.getByActiveGisuAndPart(part));
    }

    @Override
    @PutMapping
    public void manageCurriculum(
            @Valid @RequestBody ManageCurriculumRequest request
    ) {
        // TODO: user의 권한에 따라 막히게 구현 필요
        manageCurriculumUseCase.manage(request.toCommand());
    }
}
