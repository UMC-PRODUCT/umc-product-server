package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.request.CreateChapterRequest;
import com.umc.product.organization.adapter.in.web.swagger.AdminChapterControllerApi;
import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chapters")
@RequiredArgsConstructor
public class AdminChapterController implements AdminChapterControllerApi {

    private final ManageChapterUseCase manageChapterUseCase;

    @Public
    @Override
    @PostMapping
    public Long createChapter(@RequestBody @Valid CreateChapterRequest request) {
        return manageChapterUseCase.create(request.toCommand());
    }

    @Public
    @Override
    @PostMapping("/bulk")
    public List<Long> createChapterBulk(@RequestBody List<CreateChapterRequest> requests) {
        return requests.stream()
            .map(CreateChapterRequest::toCommand)
            .map(manageChapterUseCase::create)
            .toList();
    }


    @Public
    @Override
    @DeleteMapping("/{chapterId}")
    public void deleteChapter(@PathVariable Long chapterId) {
        manageChapterUseCase.delete(chapterId);
    }
}
