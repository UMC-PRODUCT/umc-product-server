package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;

public interface ManageSchoolUseCase {

    void register(CreateSchoolCommand command);


    void updateSchool(UpdateSchoolCommand command);

    void deleteSchool(Long schoolId);
//
//    void deleteAll(List<Long> schoolIds);
}
