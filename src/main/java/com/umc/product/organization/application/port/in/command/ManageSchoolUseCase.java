package com.umc.product.organization.application.port.in.command;

import com.umc.product.organization.application.port.in.command.dto.AssignSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UnassignSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import java.util.List;

public interface ManageSchoolUseCase {

    Long register(CreateSchoolCommand command);

    void updateSchool(Long schoolId, UpdateSchoolCommand command);

    void deleteSchools(List<Long> schoolIds);

    void assignToChapter(AssignSchoolCommand command);

    void unassignFromChapter(UnassignSchoolCommand command);
}
