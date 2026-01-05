package com.umc.product.command.organization.application.port.in;

import com.umc.product.command.organization.application.port.in.dto.request.CreateSchoolUseCaseRequest;
import com.umc.product.command.organization.application.port.in.dto.response.UpdateSchoolInfo;
import com.umc.product.command.organization.application.port.in.dto.request.UpdateSchoolUseCaseRequest;

import java.util.List;

public interface SchoolCommandUseCase {


    Long register(CreateSchoolUseCaseRequest request);

    void update(UpdateSchoolUseCaseRequest request);

    void deleteById(Long schoolId);
    void deleteAll(List<Long> schoolIds);
}
