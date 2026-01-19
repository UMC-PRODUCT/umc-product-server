package com.umc.product.notice.application.service.query;

import com.umc.product.notice.application.port.in.query.GetNoticeFilterUseCase;
import com.umc.product.notice.application.port.in.query.dto.NoticeScopeInfo;
import com.umc.product.notice.application.port.in.query.dto.WritableNoticeScopeOption;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeFilterQueryService implements GetNoticeFilterUseCase {

    /*
     * TODO: 권한 관련 로직은 추후 추가 예정
     */

    @Override
    public List<NoticeScopeInfo> getAvailableFilters() {

        throw new NoticeDomainException(NoticeErrorCode.NOT_IMPLEMENTED_YET);
    }

    @Override
    public WritableNoticeScopeOption getWritableNoticeScope() {

        throw new NoticeDomainException(NoticeErrorCode.NOT_IMPLEMENTED_YET);
    }
}
