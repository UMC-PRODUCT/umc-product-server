package com.umc.product.test.application.port.in.command.dto;

/**
 * 멤버 시딩 Command. ADR-017 참조.
 *
 * @param count 생성할 ID/PW 회원 수 (0 이하면 시딩 스킵)
 * @param force true 면 멱등성 임계값 체크를 무시하고 무조건 시딩
 */
public record SeedMembersCommand(
    int count,
    boolean force
) {
}
