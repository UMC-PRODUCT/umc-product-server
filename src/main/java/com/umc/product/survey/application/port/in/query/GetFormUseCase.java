package com.umc.product.survey.application.port.in.query;

import com.umc.product.survey.application.port.in.query.dto.FormInfo;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;

import java.util.Optional;

/**
 * Form 조회 UseCase.
 * <p>
 * {@link #findById}는 없을 수 있는 케이스(탈퇴 사용자의 폼 등)에 사용,
 * {@link #getById}는 반드시 존재해야 하는 케이스에 사용한다.
 * {@link #getFormWithStructure}는 편집기 초기 로딩/응답자 화면 등에서 폼 전체 구조(섹션/질문/옵션)를 한 번에 조회할 때 사용.
 */
public interface GetFormUseCase {

    /**
     * Form ID로 단건 조회. 없으면 Optional.empty.
     */
    Optional<FormInfo> findById(Long formId);

    /**
     * Form ID로 단건 조회. 없으면 SURVEY_NOT_FOUND 예외.
     */
    FormInfo getById(Long formId);

    /**
     * 폼 전체 구조(섹션 → 질문 → 옵션 중첩)를 한 번에 조회.
     * 편집기 초기 로딩이나 응답자 UI 렌더링 시 N+1 왕복을 피하기 위한 facade.
     */
    FormWithStructureInfo getFormWithStructure(Long formId);
}
