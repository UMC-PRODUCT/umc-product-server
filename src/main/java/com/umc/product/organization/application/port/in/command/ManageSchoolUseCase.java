package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import java.util.List;

public interface ManageSchoolUseCase {

    void register(CreateSchoolCommand command);

    void updateSchool(UpdateSchoolCommand command);

    void deleteSchools(List<Long> schoolIds);
}
