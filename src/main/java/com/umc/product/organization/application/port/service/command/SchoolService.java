package com.umc.product.organization.application.port.service.command;

import com.umc.product.organization.application.port.in.command.ManageSchoolUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateSchoolCommand;
import com.umc.product.organization.application.port.in.command.dto.UpdateSchoolCommand;
import org.springframework.stereotype.Service;

@Service
public class SchoolService implements ManageSchoolUseCase {


    public void register(CreateSchoolCommand command) {

    }

    public void updateSchool(UpdateSchoolCommand command) {

    }

    public void deleteSchool(Long schoolId) {

    }

    public void deleteSchools(java.util.List<Long> schoolIds) {

    }
}
