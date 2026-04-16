package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.Form;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormJpaRepository extends JpaRepository<Form, Long> {
}
