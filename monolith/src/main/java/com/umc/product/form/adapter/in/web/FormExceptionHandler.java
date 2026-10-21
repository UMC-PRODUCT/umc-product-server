package com.umc.product.form.adapter.in.web;

import java.util.Map;

import org.hibernate.StaleObjectStateException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.umc.product.form.domain.FormResponse;
import com.umc.product.form.domain.exception.DraftSchemaMismatchException;
import com.umc.product.form.domain.exception.FormErrorCode;
import com.umc.product.global.exception.GlobalExceptionHandler;
import com.umc.product.global.response.ApiErrorResponseFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 모든 REST 컨트롤러에서 발생할 수 있는 Form 전용 오류 응답을 처리한다.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor
public class FormExceptionHandler {

    private final GlobalExceptionHandler globalExceptionHandler;

    /**
     * draft 제출 시 현재 질문 구조와 맞지 않는 답변의 질문 ID를 함께 반환한다.
     */
    @ExceptionHandler(DraftSchemaMismatchException.class)
    public ResponseEntity<Object> onDraftSchemaMismatch(DraftSchemaMismatchException e) {
        log.warn("[BUSINESS EXCEPTION] domain={}, code={}, staleQuestionIds={}",
            e.getDomain(), e.getBaseCode().getCode(), e.getStaleQuestionIds());

        return ResponseEntity.status(e.getBaseCode().getHttpStatus())
            .body(ApiErrorResponseFactory.from(e.getBaseCode(), Map.of("staleQuestionIds", e.getStaleQuestionIds())));
    }

    /**
     * FormResponse 낙관적 락 실패만 409로 변환한다. 다른 엔티티의 실패는 기존 공통 처리에 위임한다.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Object> onOptimisticLockingFailure(
        ObjectOptimisticLockingFailureException e,
        WebRequest request
    ) {
        if (!isFormResponseConflict(e.getPersistentClassName())) {
            return globalExceptionHandler.handleUnhandledException(e, request);
        }
        log.warn("[FORM RESPONSE CONCURRENT MODIFICATION] persistentClass={}, id={}",
            e.getPersistentClassName(), e.getIdentifier());
        return concurrentModificationResponse();
    }

    @ExceptionHandler(StaleObjectStateException.class)
    public ResponseEntity<Object> onStaleObjectState(StaleObjectStateException e, WebRequest request) {
        if (!isFormResponseConflict(e.getEntityName())) {
            return globalExceptionHandler.handleUnhandledException(e, request);
        }
        log.warn("[FORM RESPONSE CONCURRENT MODIFICATION] entityName={}, id={}",
            e.getEntityName(), e.getIdentifier());
        return concurrentModificationResponse();
    }

    private ResponseEntity<Object> concurrentModificationResponse() {
        return ResponseEntity.status(FormErrorCode.FORM_RESPONSE_CONCURRENT_MODIFICATION.getHttpStatus())
            .body(ApiErrorResponseFactory.from(FormErrorCode.FORM_RESPONSE_CONCURRENT_MODIFICATION));
    }

    private boolean isFormResponseConflict(String className) {
        return className != null
            && (className.equals(FormResponse.class.getName()) || className.equals(FormResponse.class.getSimpleName()));
    }
}
