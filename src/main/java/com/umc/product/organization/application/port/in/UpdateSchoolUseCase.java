package com.umc.product.organization.application.port.in;

import com.umc.product.organization.application.port.in.dto.UpdateSchoolCommand;
import com.umc.product.organization.application.port.in.dto.UpdateSchoolResult;

public interface UpdateSchoolUseCase {
    UpdateSchoolResult update(UpdateSchoolCommand command);
}
