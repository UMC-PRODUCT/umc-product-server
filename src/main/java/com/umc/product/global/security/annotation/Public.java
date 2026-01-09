package com.umc.product.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 공개 API를 나타내는 어노테이션
 * <p>
 * Spring Security의 {@link PreAuthorize @PreAuthorize("permitAll()")}와 동일하게 동작합니다. 인증 없이 접근 가능한 API에 사용합니다.
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
 * @see PreAuthorize
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("permitAll()")  // Meta-annotation: @Public = @PreAuthorize("permitAll()")
public @interface Public {

}
