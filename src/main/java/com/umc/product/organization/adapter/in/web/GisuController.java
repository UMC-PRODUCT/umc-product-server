package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.dto.request.CreateGisuRequest;
import com.umc.product.organization.adapter.in.web.dto.request.UpdateGisuRequest;
import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/gisu")
@RequiredArgsConstructor
public class GisuController {

    private final ManageGisuUseCase manageGisuUseCase;

    @PostMapping
    public Long createGisu(@RequestBody @Valid CreateGisuRequest request) {
        return manageGisuUseCase.register(request.toCommand());
    }

    @PatchMapping("/{gisuId}")
    public void updateGisu(@PathVariable Long gisuId, @RequestBody @Valid UpdateGisuRequest request) {
        manageGisuUseCase.updateGisu(request.toCommand(gisuId));
    }

    @DeleteMapping("/{gisuId}")
    public void deleteGisu(@PathVariable Long gisuId) {
        manageGisuUseCase.deleteGisu(gisuId);
    }

    @PostMapping("/{gisuId}/current")
    public void setCurrentGisu(@PathVariable Long gisuId) {
        manageGisuUseCase.setCurrentGisu(gisuId);
    }
}
