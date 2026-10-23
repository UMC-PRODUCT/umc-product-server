package com.umc.product.global.response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.global.response.code.BaseCode;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse response, BaseCode code) throws IOException {
        write(response, code, code.getMessage(), null);
    }

    public void write(HttpServletResponse response, BaseCode code, Object detail) throws IOException {
        write(response, code, code.getMessage(), detail);
    }

    public void write(HttpServletResponse response, BaseCode code, String message, Object detail) throws IOException {
        response.setStatus(code.getHttpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiErrorResponseFactory.from(code, message, detail));
    }
}
