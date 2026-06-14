package com.umc.product.organization.application.port.service.query;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuNameInfo;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GisuQueryService implements GetGisuUseCase {

    private final LoadGisuPort loadGisuPort;

    @Override
    public List<GisuInfo> getList() {
        return loadGisuPort.findAll().stream()
            .map(GisuInfo::from)
            .toList();
    }

    @Override
    public Page<GisuInfo> getList(Pageable pageable) {
        return loadGisuPort.findAll(pageable)
            .map(GisuInfo::from);
    }

    @Override
    public List<GisuNameInfo> getAllGisuNames() {
        return loadGisuPort.findAll().stream()
            .map(GisuNameInfo::from)
            .toList();
    }

    @Override
    public GisuInfo getById(Long gisuId) {
        return GisuInfo.from(loadGisuPort.getById(gisuId));
    }

    @Override
    public List<GisuInfo> getByIds(Set<Long> gisuIds) {
        return loadGisuPort.listByIds(gisuIds).stream()
            .map(GisuInfo::from)
            .toList();
    }

    @Override
    public List<GisuInfo> batchGetByIds(List<Long> gisuIds) {
        List<Long> uniqueIds = unique(gisuIds);
        if (uniqueIds.isEmpty()) {
            return List.of();
        }

        Map<Long, GisuInfo> gisuById = loadGisuPort.listByIds(new LinkedHashSet<>(uniqueIds)).stream()
            .map(GisuInfo::from)
            .collect(Collectors.toMap(GisuInfo::gisuId, Function.identity()));

        validateAllExist(uniqueIds, gisuById);
        return uniqueIds.stream()
            .map(gisuById::get)
            .toList();
    }

    @Override
    public List<GisuInfo> batchGetByGenerations(List<Long> generations) {
        List<Long> uniqueGenerations = unique(generations);
        if (uniqueGenerations.isEmpty()) {
            return List.of();
        }

        Map<Long, GisuInfo> gisuByGeneration = loadGisuPort.listByGenerations(new LinkedHashSet<>(uniqueGenerations))
            .stream()
            .map(GisuInfo::from)
            .collect(Collectors.toMap(GisuInfo::generation, Function.identity()));

        validateAllExist(uniqueGenerations, gisuByGeneration);
        return uniqueGenerations.stream()
            .map(gisuByGeneration::get)
            .toList();
    }

    @Override
    public Long getActiveGisuId() {
        return loadGisuPort.getActiveGisu().getId();
    }

    @Override
    public GisuInfo getActiveGisu() {
        return GisuInfo.from(loadGisuPort.getActiveGisu());
    }

    @Override
    public Optional<GisuInfo> findActiveGisu() {
        return loadGisuPort.findActiveGisu().map(GisuInfo::from);
    }

    @Override
    public GisuInfo getGisuByDate(Instant targetDate) {
        return GisuInfo.from(
            loadGisuPort.findGisuByDate(targetDate)
                .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.GISU_NOT_FOUND))
        );
    }

    private List<Long> unique(List<Long> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private void validateAllExist(List<Long> requestedValues, Map<Long, GisuInfo> resultByValue) {
        if (resultByValue.size() != requestedValues.size()) {
            throw new OrganizationDomainException(OrganizationErrorCode.GISU_NOT_FOUND);
        }
    }
}
