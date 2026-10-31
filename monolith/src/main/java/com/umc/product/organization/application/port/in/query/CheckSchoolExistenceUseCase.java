package com.umc.product.organization.application.port.in.query;

public interface CheckSchoolExistenceUseCase {

    boolean existsById(Long schoolId);
}
