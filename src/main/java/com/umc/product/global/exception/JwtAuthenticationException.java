package com.umc.product.global.exception;


import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {
    public JwtAuthenticationException(AuthenticationDomainException e) {
        super(e.getMessage(), e);
    }

    public JwtAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
