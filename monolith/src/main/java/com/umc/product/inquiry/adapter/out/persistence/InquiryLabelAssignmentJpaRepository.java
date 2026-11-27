package com.umc.product.inquiry.adapter.out.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.inquiry.domain.InquiryLabelAssignment;

public interface InquiryLabelAssignmentJpaRepository extends JpaRepository<InquiryLabelAssignment, Long> {

    List<InquiryLabelAssignment> findAllByInquiryIdIn(Collection<Long> inquiryIds);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("delete from InquiryLabelAssignment assignment where assignment.inquiryId = :inquiryId")
    void deleteAllByInquiryId(@Param("inquiryId") Long inquiryId);
}
