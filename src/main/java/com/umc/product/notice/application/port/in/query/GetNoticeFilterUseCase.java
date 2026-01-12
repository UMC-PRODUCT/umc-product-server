package com.umc.product.notice.application.port.in.query;

import com.umc.product.notice.application.port.in.query.dto.NoticeScopeInfo;
import com.umc.product.notice.application.port.in.query.dto.WritableNoticeScopeOption;
import java.util.List;

public interface GetNoticeFilterUseCase {

    /*
    * 로그인 한 사용자의 조직에 따른 동적 필터 반환
    * @return 공지 scope 리스트 반환 (ex| 전체, 중앙, 한양대학교 ERICA, springboot)
    */
    List<NoticeScopeInfo> getAvailableFilters(Long challengerId);

    /*
    * 로그인 한 사용자의 role에 따른 작성 가능 카테고리 반환
    * @return 작성 가능 공지 카테고리 반환
    */
    WritableNoticeScopeOption getWritableNoticeScope(Long challengerId);
}
