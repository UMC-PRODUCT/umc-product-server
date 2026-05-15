package com.umc.product.test.application.port.in.command.dto;

/**
 * 멤버 시딩 결과. ADR-017 참조.
 *
 * @param registeredIdPw  실제로 등록된 ID/PW 회원 수
 * @param registeredOAuth 실제로 등록된 OAuth 회원 수
 * @param skipped         멱등성 임계값으로 스킵된 경우 true
 * @param reason          스킵 사유 (skipped=false 면 null)
 */
public record SeedMembersResult(
    int registeredIdPw,
    int registeredOAuth,
    boolean skipped,
    String reason
) {

    public static SeedMembersResult skipped(String reason) {
        return new SeedMembersResult(0, 0, true, reason);
    }

    public static SeedMembersResult of(int registeredIdPw, int registeredOAuth) {
        return new SeedMembersResult(registeredIdPw, registeredOAuth, false, null);
    }
}
