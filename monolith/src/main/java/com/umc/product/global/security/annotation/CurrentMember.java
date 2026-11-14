package com.umc.product.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * 요청한 회원의 {@link com.umc.product.global.security.MemberPrincipal} 을 주입한다.
 * <p>
 * REST(MVC) 요청에서는 로그인하지 않았으면 401, 데모데이 게스트처럼 회원이 아닌 인증이면 403 으로 응답한다.
 * 비로그인도 허용하는 {@code @Public} API 만 {@code required = false} 로 두고 null 을 받는다.
 * GraphQL 은 Spring GraphQL 해석기가 값을 넣어 {@code required} 를 읽지 않으므로, 파라미터를 {@code @Nullable} 로 두고 본문에서
 * 로그인을 확인한다.
 */
@Target(ElementType.PARAMETER) // 파라미터에만 붙일 수 있음
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지
@AuthenticationPrincipal // GraphQL 이 이 메타 어노테이션으로 회원 정보를 넣는다
public @interface CurrentMember {

    boolean required() default true;
}
