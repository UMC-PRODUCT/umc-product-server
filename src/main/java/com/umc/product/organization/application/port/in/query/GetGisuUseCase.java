package com.umc.product.organization.application.port.in.query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuNameInfo;

public interface GetGisuUseCase {

    List<GisuInfo> getList();

    Page<GisuInfo> getList(Pageable pageable);

    List<GisuNameInfo> getAllGisuNames();

    GisuInfo getById(Long gisuId);

    List<GisuInfo> getByIds(Set<Long> gisuIds);

    List<GisuInfo> batchGetByIds(List<Long> gisuIds);

    List<GisuInfo> batchGetByGenerations(List<Long> generations);

    Long getActiveGisuId();

    GisuInfo getActiveGisu();

    /**
     * 활성 기수가 존재하지 않을 수 있는 시점(휴지기 등)에 사용합니다.
     */
    Optional<GisuInfo> findActiveGisu();

    GisuInfo getGisuByDate(Instant targetDate);
}
