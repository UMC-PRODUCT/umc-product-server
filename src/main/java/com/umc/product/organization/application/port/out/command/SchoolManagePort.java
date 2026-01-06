package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.School;

public interface SchoolManagePort {

    School save(School school);

    void delete(School school);
}
