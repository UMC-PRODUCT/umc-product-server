package com.umc.product.organization.application.port.out.command;


import com.umc.product.organization.domain.School;
import java.util.List;

public interface ManageSchoolPort {

    School save(School school);

    void deleteAllByIds(List<Long> schoolIds);

    void deleteAllLinksBySchoolIds(List<Long> schoolIds);
}
