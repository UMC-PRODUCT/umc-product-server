package com.umc.product.global.security.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.umc.product.global.security.CurrentMemberProvider;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final CurrentMemberProvider currentMemberProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(CurrentMember.class);
        boolean hasUserType = MemberPrincipal.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && hasUserType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        MemberPrincipal memberPrincipal = currentMemberProvider.getNullableCurrentMember();
        if (memberPrincipal == null && !parameter.isOptional() && currentMemberProvider.isAnonymous()) {
            throw new AuthenticationCredentialsNotFoundException("로그인 정보가 없는 요청입니다.");
        }
        return memberPrincipal;
    }
}
