package com.umc.product.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Target(ElementType.PARAMETER) // 파라미터에만 붙일 수 있음
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지
@AuthenticationPrincipal // (선택사항) Swagger 등이 이 어노테이션을 인식하도록 돕는 메타 어노테이션
public @interface CurrentMember {
}
