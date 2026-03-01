package com.umc.product.organization.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
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

    @CheckAccess(resourceType = ResourceType.CHAPTER, permission = PermissionType.WRITE)
    @Override
    @PostMapping
    public Long createChapter(@RequestBody @Valid CreateChapterRequest request) {
        return manageChapterUseCase.create(request.toCommand());
    }

//    @CheckAccess(resourceType = ResourceType.CHAPTER, permission = PermissionType.WRITE)
    @Override
    @PostMapping("/bulk")
    public List<Long> createChapterBulk(@RequestBody List<CreateChapterRequest> requests) {
        return requests.stream()
            .map(CreateChapterRequest::toCommand)
            .map(manageChapterUseCase::create)
            .toList();
    }

    @CheckAccess(resourceType = ResourceType.CHAPTER, permission = PermissionType.DELETE)
    @Override
    @DeleteMapping("/{chapterId}")
    public void deleteChapter(@PathVariable Long chapterId) {
        manageChapterUseCase.delete(chapterId);
    }
}
