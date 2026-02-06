package com.umc.product.organization.adapter.in.web;

import com.umc.product.global.security.annotation.Public;
import com.umc.product.organization.adapter.in.web.dto.request.CreateGisuRequest;
import com.umc.product.organization.application.port.in.command.ManageGisuUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gisu")
@RequiredArgsConstructor
public class AdminGisuController implements AdminGisuControllerApi {

    private final ManageGisuUseCase manageGisuUseCase;

    @Public
    @Override
    @PostMapping
    public Long createGisu(@Valid @RequestBody CreateGisuRequest request) {
        return manageGisuUseCase.register(request.toCommand());
    }

    @Public
    @Override
    @DeleteMapping("/{gisuId}")
    public void deleteGisu(@PathVariable Long gisuId) {
        manageGisuUseCase.deleteGisu(gisuId);
    }

    @Public
    @Override
    @PostMapping("/{gisuId}/active")
    public void updateActiveGisu(@PathVariable Long gisuId) {
        manageGisuUseCase.updateActiveGisu(gisuId);
    }
}
