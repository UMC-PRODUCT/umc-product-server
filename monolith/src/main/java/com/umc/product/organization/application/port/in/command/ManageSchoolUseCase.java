package com.umc.product.organization.application.port.in.command;

import java.util.List;

import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;

public interface ManageSchoolUseCase {

    Long create(CreateSchoolCommand command);

    void updateSchool(Long schoolId, UpdateSchoolCommand command);

    void deleteSchools(List<Long> schoolIds);

}
