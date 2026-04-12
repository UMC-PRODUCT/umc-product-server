package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.FormSection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormSectionJpaRepository extends JpaRepository<FormSection, Long> {
}
