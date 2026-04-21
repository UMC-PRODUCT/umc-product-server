package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.FormSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormSectionJpaRepository extends JpaRepository<FormSection, Long> {

    List<FormSection> findAllByFormId(Long formId);
}
