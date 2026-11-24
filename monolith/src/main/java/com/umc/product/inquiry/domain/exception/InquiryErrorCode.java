package com.umc.product.inquiry.domain.exception;

import org.springframework.http.HttpStatus;

import com.umc.product.global.response.code.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryErrorCode implements BaseCode {

    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY-0001", "문의를 찾을 수 없어요. 목록을 새로고침해주세요."),
    INQUIRY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "INQUIRY-0002", "이 문의를 볼 수 있는 권한이 없어요."),
    INQUIRY_STAFF_ONLY(HttpStatus.FORBIDDEN, "INQUIRY-0003", "UMC PRODUCT 운영진만 할 수 있어요."),
    INQUIRY_ALREADY_CLOSED(HttpStatus.CONFLICT, "INQUIRY-0004", "이미 종료된 문의예요. 목록을 새로고침해주세요."),
    INQUIRY_CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "INQUIRY-0005", "선택한 문의 유형을 찾을 수 없어요. 유형을 다시 선택해주세요."),
    INVALID_INQUIRY_TITLE(HttpStatus.BAD_REQUEST, "INQUIRY-0006", "문의 제목은 1자 이상 100자 이하로 입력해주세요."),
    INVALID_INQUIRY_CONTENT(HttpStatus.BAD_REQUEST, "INQUIRY-0007", "내용은 2,000자 이하로 입력해주세요."),
    INQUIRY_MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "INQUIRY-0008", "보낼 내용이나 첨부파일을 추가해주세요."),
    INVALID_INQUIRY_ATTACHMENT(HttpStatus.BAD_REQUEST, "INQUIRY-0009", "문의 첨부파일로 올린 파일만 첨부할 수 있어요. 파일을 다시 올려주세요."),
    INQUIRY_ATTACHMENT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "INQUIRY-0010", "첨부파일은 5개까지 올릴 수 있어요."),
    INQUIRY_REQUEST_CONFLICT(HttpStatus.CONFLICT, "INQUIRY-0011", "같은 문의를 처리하고 있어요. 잠시 후 다시 시도해주세요."),
    INQUIRY_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "INQUIRY-0012", "문의 내용을 입력해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
