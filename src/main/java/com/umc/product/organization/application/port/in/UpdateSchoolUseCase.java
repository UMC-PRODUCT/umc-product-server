package com.umc.product.organization.application.port.in;

import com.umc.product.organization.application.port.in.dto.UpdateSchoolCommand;
import com.umc.product.organization.application.port.in.dto.UpdateSchoolInfo;

public interface UpdateSchoolUseCase {
    UpdateSchoolInfo update(UpdateSchoolCommand command);
}
