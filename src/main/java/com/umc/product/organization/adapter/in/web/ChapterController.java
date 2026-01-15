package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.dto.request.CreateChapterRequest;
import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ManageChapterUseCase manageChapterUseCase;

    @PostMapping
    public void createChapter(@RequestBody @Valid CreateChapterRequest request) {
        manageChapterUseCase.create(request.toCommand());
    }
}
