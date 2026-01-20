package com.umc.product.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 공개 API를 나타내는 어노테이션
 * <p>
 * 인증 없이 접근 가능한 API에 사용합니다. SecurityConfig에서 PublicEndpointCollector가 이 어노테이션을 스캔하여 자동으로 permitAll()을 적용합니다.
 * </p>
 *
 * <h3>사용 예시:</h3>
 * <pre>
 * {@code
 * @GetMapping("/posts")
 * @Public
 * public ApiResponse<?> getPublicPosts() {
 *     // 인증 없이 접근 가능
 * }
 * }
 * </pre>
 *
 * @see com.umc.product.global.security.util.PublicEndpointCollector
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Public {

}
