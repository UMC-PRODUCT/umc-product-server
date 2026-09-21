package com.umc.product.challenger.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.challenger.adapter.in.web.dto.response.PartResponse;
import com.umc.product.common.domain.enums.ChallengerPart;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/parts")
@Tag(name = "Part | 파트", description = "파트 정보를 조회합니다.")
public class PartQueryController {

    @GetMapping
    @Operation(operationId = "PART-101", summary = "선택 가능한 파트 목록 조회",
        description = "신규 발급 대상 파트만 sortOrder 순으로 반환합니다. 레거시 파트는 제외됩니다.")
    List<PartResponse> getSelectableParts() {
        return ChallengerPart.selectableValues().stream()
            .map(PartResponse::from)
            .toList();
    }
}
