package com.umc.product.global.security;

import java.util.List;
import lombok.Builder;

@Builder
public record UserPrincipal(
        Long userId,
        List<String> roles
) {

}