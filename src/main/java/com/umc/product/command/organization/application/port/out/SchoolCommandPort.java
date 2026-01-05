package com.umc.product.command.organization.application.port.out;

import com.umc.product.command.organization.domain.School;

public interface SchoolCommandPort {

    School save(School school);

    void delete(School school);
}
