package com.umc.product.notice.application.port.in.query.dto;

/**
 * 공지사항 상세 -
 *
 * @param id
 * @param name
 */
public record NoticeDetailOrganizationOption(
    /*
     * id: 조직 ID, name: 조직 이름
     */
    Long id,
    String name
) {
}
