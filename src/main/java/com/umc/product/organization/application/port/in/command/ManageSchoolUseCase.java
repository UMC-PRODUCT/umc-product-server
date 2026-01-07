package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;

public interface ManageSchoolUseCase {

    void register(CreateSchoolCommand command);

//
//    void update(UpdateSchoolCommand command);
//
//    void delete(Long schoolId);
//
//    void deleteAll(List<Long> schoolIds);
}
