package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/chapters")
@RequiredArgsConstructor
@Tag(name = Constants.ORGANIZATION)
public class ChapterController {

    private final ManageChapterUseCase manageChapterUseCase;

//    @PostMapping
//    public void createChapter(@RequestBody @Valid CreateChapterRequest request) {
//        manageChapterUseCase.create(request.toCommand());
//    }
}
