package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.FormSection;
import com.umc.product.survey.domain.enums.FormSectionType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormSectionJpaRepository extends JpaRepository<FormSection, Long> {
    Optional<FormSection> findByFormIdAndTypeAndOrderNo(Long formId, FormSectionType type, Integer orderNo);

    Optional<FormSection> findByFormIdAndTypeAndTargetKey(Long formId, FormSectionType type, String targetKey);
}
