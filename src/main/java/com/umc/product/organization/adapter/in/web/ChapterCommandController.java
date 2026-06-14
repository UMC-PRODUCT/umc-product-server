package com.umc.product.organization.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.organization.adapter.in.web.dto.request.CreateChapterRequest;
import com.umc.product.organization.adapter.in.web.swagger.AdminChapterControllerApi;
import com.umc.product.organization.application.port.in.command.ManageChapterUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chapters")
@RequiredArgsConstructor
public class ChapterCommandController implements AdminChapterControllerApi {

    private final ManageChapterUseCase manageChapterUseCase;

    @CheckAccess(
        resourceType = ResourceType.CHAPTER,
        permission = PermissionType.WRITE,
        message = "지부를 만들 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @Override
    @PostMapping
    public Long createChapter(@RequestBody @Valid CreateChapterRequest request) {
        return manageChapterUseCase.create(request.toCommand());
    }

    @CheckAccess(
        resourceType = ResourceType.CHAPTER,
        permission = PermissionType.WRITE,
        message = "지부를 만들 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @Override
    @PostMapping("/bulk")
    public List<Long> createChapterBulk(@RequestBody List<CreateChapterRequest> requests) {
        return requests.stream()
            .map(CreateChapterRequest::toCommand)
            .map(manageChapterUseCase::create)
            .toList();
    }

    @CheckAccess(
        resourceType = ResourceType.CHAPTER,
        permission = PermissionType.DELETE,
        message = "지부를 삭제할 권한이 없어요. 필요한 권한이 있다면 운영진에게 문의해주세요."
    )
    @Override
    @DeleteMapping("/{chapterId}")
    public void deleteChapter(@PathVariable Long chapterId) {
        manageChapterUseCase.delete(chapterId);
    }
}
