package com.umc.product.inquiry.adapter.out.persistence;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.umc.product.global.persistence.ConstraintViolationInspector;
import com.umc.product.inquiry.application.port.out.LoadInquiryLabelPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryLabelPort;
import com.umc.product.inquiry.domain.InquiryLabel;
import com.umc.product.inquiry.domain.InquiryLabelAssignment;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryLabelPersistenceAdapter implements LoadInquiryLabelPort, SaveInquiryLabelPort {

    private static final String NAME_CONSTRAINT = "uq_inquiry_label_name";
    private static final Comparator<InquiryLabel> BY_NAME =
        Comparator.comparing(InquiryLabel::getName).thenComparing(InquiryLabel::getId);

    private final InquiryLabelJpaRepository labelRepository;
    private final InquiryLabelAssignmentJpaRepository assignmentRepository;

    @Override
    public List<InquiryLabel> listAll() {
        return labelRepository.findAllByOrderByNameAscIdAsc();
    }

    @Override
    public Optional<InquiryLabel> findById(Long labelId) {
        return labelRepository.findById(labelId);
    }

    @Override
    public List<InquiryLabel> listByIds(Collection<Long> labelIds) {
        return labelIds.isEmpty() ? List.of() : labelRepository.findAllById(labelIds);
    }

    @Override
    public Map<Long, List<InquiryLabel>> listByInquiryIds(Collection<Long> inquiryIds) {
        if (inquiryIds.isEmpty()) {
            return Map.of();
        }
        List<InquiryLabelAssignment> assignments = assignmentRepository.findAllByInquiryIdIn(inquiryIds);
        Map<Long, InquiryLabel> labels = listByIds(
            assignments.stream().map(InquiryLabelAssignment::getLabelId).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(InquiryLabel::getId, Function.identity()));
        return assignments.stream()
            .filter(assignment -> labels.containsKey(assignment.getLabelId()))
            .collect(Collectors.groupingBy(
                InquiryLabelAssignment::getInquiryId,
                Collectors.collectingAndThen(
                    Collectors.mapping(assignment -> labels.get(assignment.getLabelId()), Collectors.toList()),
                    list -> list.stream().sorted(BY_NAME).toList()
                )
            ));
    }

    @Override
    public InquiryLabel save(InquiryLabel label) {
        try {
            return labelRepository.saveAndFlush(label);
        } catch (DataIntegrityViolationException exception) {
            if (ConstraintViolationInspector.matches(exception, NAME_CONSTRAINT)) {
                throw new InquiryDomainException(InquiryErrorCode.INQUIRY_LABEL_NAME_DUPLICATED, exception);
            }
            throw exception;
        }
    }

    @Override
    public void delete(InquiryLabel label) {
        labelRepository.delete(label);
    }

    @Override
    public void replaceAssignments(Long inquiryId, Collection<Long> labelIds) {
        assignmentRepository.deleteAllByInquiryId(inquiryId);
        assignmentRepository.saveAll(
            labelIds.stream().map(labelId -> InquiryLabelAssignment.create(inquiryId, labelId)).toList()
        );
    }
}
