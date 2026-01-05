package com.umc.product.organization.application.port.in;

import java.util.List;

public interface DeleteSchoolUseCase {
    void deleteById(Long schoolId);

    void deleteAll(List<Long> schoolIds);
}
