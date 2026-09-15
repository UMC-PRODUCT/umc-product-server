package com.umc.product.organization.application.port.out.command;


import java.util.List;

import com.umc.product.organization.domain.School;

public interface SaveSchoolPort {

    School save(School school);

    void deleteAllByIds(List<Long> schoolIds);

    void deleteAllLinksBySchoolIds(List<Long> schoolIds);
}
