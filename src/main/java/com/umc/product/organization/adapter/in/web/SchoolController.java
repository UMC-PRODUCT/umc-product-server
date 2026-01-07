package com.umc.product.organization.adapter.in.web;

import com.umc.product.organization.adapter.in.web.dto.request.CreateSchoolRequest;
import com.umc.product.organization.application.port.in.command.ManageSchoolUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final ManageSchoolUseCase manageSchoolUseCase;

    @PostMapping()
    public void createSchool(
            @RequestBody @Valid CreateSchoolRequest createSchoolRequest
    ) {

        manageSchoolUseCase.register(createSchoolRequest.toCommand());


    }
}

