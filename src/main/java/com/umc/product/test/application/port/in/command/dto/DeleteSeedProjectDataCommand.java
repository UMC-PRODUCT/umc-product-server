package com.umc.product.test.application.port.in.command.dto;

/**
 * 테스트 프로젝트 데이터 삭제 Command.
 *
 * @param gisuId 대상 기수 ID. null 이면 활성 기수를 사용한다.
 */
public record DeleteSeedProjectDataCommand(
    Long gisuId
) {

    public static DeleteSeedProjectDataCommand of(Long gisuId) {
        return new DeleteSeedProjectDataCommand(gisuId);
    }
}
