package com.umc.product.global.security;

import lombok.Builder;

import java.util.List;

@Builder
public record UserPrincipal(
        Long userId,
        List<String> roles
) {

}