package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.dto.request.CreateStudyGroupRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateStudyGroupRequest;
import com.umc.product.organization.application.port.in.command.ManageStudyGroupUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/study-groups")
@RequiredArgsConstructor
public class StudyGroupCommandController implements StudyGroupCommandControllerApi {

    private final ManageStudyGroupUseCase manageStudyGroupUseCase;

    @Override
    @PostMapping
    public void create(@Valid @RequestBody CreateStudyGroupRequest request) {
        manageStudyGroupUseCase.create(request.toCommand());
    }

    @Override
    @PutMapping("/{groupId}")
    public void update(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateStudyGroupRequest request) {
        manageStudyGroupUseCase.update(request.toCommand(groupId));
    }

    @Override
    @DeleteMapping("/{groupId}")
    public void delete(@PathVariable Long groupId) {
        manageStudyGroupUseCase.delete(groupId);
    }
}
