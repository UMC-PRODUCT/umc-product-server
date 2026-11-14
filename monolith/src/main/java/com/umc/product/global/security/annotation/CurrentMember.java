package com.umc.product.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 요청한 회원의 {@link com.umc.product.global.security.MemberPrincipal} 을 주입한다.
 * <p>
 * 인증 정보가 없는 요청이면 401 로 응답한다. 비로그인도 허용하는 API 는 파라미터에 {@code @Nullable} 을 붙여 null 을 받는다.
 * 데모데이 게스트처럼 회원이 아닌 인증은 null 을 받는다.
 */
@Target(ElementType.PARAMETER) // 파라미터에만 붙일 수 있음
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지
@AuthenticationPrincipal // (선택사항) Swagger 등이 이 어노테이션을 인식하도록 돕는 메타 어노테이션
public @interface CurrentMember {
}
