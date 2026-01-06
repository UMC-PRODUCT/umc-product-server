package com.umc.product.global.security.resolver;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 1. 파라미터에 @CurrentUser 어노테이션이 붙어 있는지 확인
        boolean hasAnnotation = parameter.hasParameterAnnotation(CurrentMember.class);
        // 2. 파라미터 타입이 UserPrincipal인지 확인
        boolean hasUserType = MemberPrincipal.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && hasUserType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나, 익명 사용자("anonymousUser" 문자열)인 경우 null 반환
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        // 실제 UserPrincipal 객체 반환
        Object principal = authentication.getPrincipal();
        if (principal instanceof MemberPrincipal) {
            return principal;
        }

        return null;
    }
}