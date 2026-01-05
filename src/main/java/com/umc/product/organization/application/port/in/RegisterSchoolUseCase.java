package com.umc.product.organization.application.port.in;

import com.umc.product.organization.application.port.in.dto.RegisterSchoolCommand;

public interface RegisterSchoolUseCase {
    Long register(RegisterSchoolCommand command);
}
