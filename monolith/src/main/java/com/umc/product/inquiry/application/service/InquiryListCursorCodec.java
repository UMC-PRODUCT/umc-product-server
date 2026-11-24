package com.umc.product.inquiry.application.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;

import com.umc.product.inquiry.application.port.out.LoadInquiryPort.Cursor;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

/**
 * 내 문의 목록 커서를 클라이언트에 내려 줄 불투명 문자열로 바꾼다. 커서가 정렬 키를 그대로 담아야 커서 문의에
 * 새 메시지가 와서 정렬 위치가 바뀌어도 다음 페이지가 겹치지 않는다.
 */
final class InquiryListCursorCodec {

    private static final String SEPARATOR = "|";

    private InquiryListCursorCodec() {
    }

    static String encode(Cursor cursor) {
        String raw = cursor.lastMessageAt() + SEPARATOR + cursor.inquiryId();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static Cursor decode(String encoded) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            int separator = raw.indexOf(SEPARATOR);
            Instant lastMessageAt = Instant.parse(raw.substring(0, separator));
            long inquiryId = Long.parseLong(raw.substring(separator + 1));
            if (inquiryId <= 0) {
                throw invalidCursor();
            }
            return new Cursor(lastMessageAt, inquiryId);
        } catch (IllegalArgumentException | DateTimeParseException | IndexOutOfBoundsException exception) {
            throw invalidCursor();
        }
    }

    private static InquiryDomainException invalidCursor() {
        return new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_CURSOR);
    }
}
