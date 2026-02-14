package com.umc.product.authorization.adapter.in.web;

import com.umc.product.authorization.adapter.in.web.dto.response.ResourcePermissionResponse;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.exception.NotImplementedException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "리소스 접근 권한 확인", description = "운영진 권한 관련 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/authorization/resource-permission")
public class ResourcePermissionController {

    @GetMapping
    ResourcePermissionResponse getResourcePermission(
        ResourceType resourceType, Long resourceId
    ) {
        throw new NotImplementedException("조금만 기다려주세요! 금방 할께요!");
    }
}
