package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import java.util.List;

public interface ManageSchoolUseCase {

    Long create(CreateSchoolCommand command);

    void update(UpdateSchoolCommand command);

    void delete(Long schoolId);

    void deleteAll(List<Long> schoolIds);
}
