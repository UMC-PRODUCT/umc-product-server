package com.umc.product.organization.application.port.out;

import com.umc.product.organization.domain.School;

public interface SaveSchoolPort {

    School save(School school);

    void delete(School school);
}
