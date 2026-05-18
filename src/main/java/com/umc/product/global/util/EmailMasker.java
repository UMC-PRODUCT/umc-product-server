package com.umc.product.global.util;

/**
 * 외부에 노출되는 응답에서 이메일을 부분 마스킹하기 위한 유틸리티입니다.
 * <p>
 * 이메일은 로그인 식별자이므로 검색 결과 등 본인 외 응답에는 원문을 그대로 노출하지 않고,
 * 로컬 파트의 앞부분 일부만 남기고 가립니다. 도메인 파트는 그대로 둡니다.
 * <p>
 * 마스킹 규칙:
 * <p>
 * - 로컬 파트 길이 1: `a@domain` (가릴 글자 없음 — 원문 유지)
 * <p>
 * - 로컬 파트 길이 2~3: 앞 1글자 + 나머지 `*` (예: `ab@x` → `a*@x`)
 * <p>
 * - 로컬 파트 길이 4 이상: 앞 3글자 + 나머지 `*` (예: `donggukcd200@x` → `don*********@x`)
 */
public final class EmailMasker {

    private EmailMasker() {
    }

    public static String mask(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }

        int at = email.indexOf('@');
        if (at <= 0) {
            // '@'가 없거나 로컬 파트가 비어있는 비정상 입력은 방어적으로 원문을 그대로 둡니다.
            return email;
        }

        String local = email.substring(0, at);
        String domain = email.substring(at);

        int keep = local.length() <= 3 ? 1 : 3;
        if (local.length() <= keep) {
            return email;
        }

        return local.substring(0, keep) + "*".repeat(local.length() - keep) + domain;
    }
}
