package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.organization.adapter.in.web.dto.request.CreateChapterRequest;
import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/chapters")
@RequiredArgsConstructor
@Tag(name = Constants.ORGANIZATION)
public class ChapterController {

    private final ManageChapterUseCase manageChapterUseCase;

    @PostMapping
    @Operation(summary = "지부 생성", description = "새로운 지부를 생성합니다. 소속 학교를 함께 지정할 수 있습니다.")
    public Long createChapter(@RequestBody @Valid CreateChapterRequest request) {
        return manageChapterUseCase.create(request.toCommand());
    }
}
