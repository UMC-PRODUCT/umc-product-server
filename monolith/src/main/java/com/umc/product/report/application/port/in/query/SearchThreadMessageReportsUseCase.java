package com.umc.product.report.application.port.in.query;

import com.umc.product.report.application.port.in.query.dto.SearchThreadMessageReportsQuery;
import com.umc.product.report.application.port.in.query.dto.ThreadMessageReportPageResult;

/**
 * 스레드 메시지 신고 목록을 조회합니다.
 * <p>
 * 조회 권한 확인과 기본 필터 같은 화면 정책은 호출하는 도메인이 정합니다.
 */
public interface SearchThreadMessageReportsUseCase {

    ThreadMessageReportPageResult search(SearchThreadMessageReportsQuery query);
}
