package com.umc.product.form.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.form.domain.FormResponse;
import com.umc.product.form.domain.exception.DraftSchemaMismatchException;
import com.umc.product.form.domain.exception.FormDomainException;
import com.umc.product.form.domain.exception.FormErrorCode;
import com.umc.product.global.exception.GlobalExceptionHandler;
import com.umc.product.global.exception.constant.CommonErrorCode;

class FormExceptionHandlerTest {

    private final GlobalExceptionHandler globalHandler = new GlobalExceptionHandler();
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new ExceptionController())
        .setControllerAdvice(globalHandler, new FormExceptionHandler(globalHandler))
        .setMessageConverters(new MappingJackson2HttpMessageConverter())
        .build();

    @Test
    @DisplayName("draft 구조 변경 오류는 재확인이 필요한 질문 ID를 반환한다")
    void draftSchemaMismatchIncludesQuestionIds() throws Exception {
        mockMvc.perform(get("/form-exception/draft"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(FormErrorCode.DRAFT_SCHEMA_MISMATCH.getCode()))
            .andExpect(jsonPath("$.result.staleQuestionIds[0]").value(11))
            .andExpect(jsonPath("$.result.staleQuestionIds[1]").value(12));
    }

    @Test
    @DisplayName("FormResponse의 Spring 낙관적 락 오류는 Form 충돌 코드로 반환한다")
    void springOptimisticLockingFailureUsesFormCode() throws Exception {
        mockMvc.perform(get("/form-exception/spring-lock"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(FormErrorCode.FORM_RESPONSE_CONCURRENT_MODIFICATION.getCode()));
    }

    @Test
    @DisplayName("FormResponse의 Hibernate 낙관적 락 오류도 Form 충돌 코드로 반환한다")
    void hibernateOptimisticLockingFailureUsesFormCode() throws Exception {
        mockMvc.perform(get("/form-exception/hibernate-lock"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(FormErrorCode.FORM_RESPONSE_CONCURRENT_MODIFICATION.getCode()));
    }

    @Test
    @DisplayName("다른 엔티티의 낙관적 락 오류는 공통 예외 처리 결과를 유지한다")
    void otherEntityOptimisticLockingFailureUsesGlobalHandler() throws Exception {
        mockMvc.perform(get("/form-exception/other-lock"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value(CommonErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }

    @Test
    @DisplayName("일반 Form 업무 예외는 기존 공통 BusinessException 처리를 사용한다")
    void ordinaryFormBusinessExceptionUsesGlobalHandler() throws Exception {
        mockMvc.perform(get("/form-exception/business"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(FormErrorCode.FORM_NOT_FOUND.getCode()));
    }

    @RestController
    private static class ExceptionController {

        @GetMapping("/form-exception/draft")
        String draft() {
            throw new DraftSchemaMismatchException(List.of(11L, 12L));
        }

        @GetMapping("/form-exception/spring-lock")
        String springLock() {
            throw new ObjectOptimisticLockingFailureException(FormResponse.class, 1L);
        }

        @GetMapping("/form-exception/hibernate-lock")
        String hibernateLock() {
            throw new StaleObjectStateException(FormResponse.class.getName(), 1L);
        }

        @GetMapping("/form-exception/other-lock")
        String otherLock() {
            throw new ObjectOptimisticLockingFailureException("ProjectApplication", 1L);
        }

        @GetMapping("/form-exception/business")
        String business() {
            throw new FormDomainException(FormErrorCode.FORM_NOT_FOUND);
        }
    }
}
