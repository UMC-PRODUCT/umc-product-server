package com.umc.product.notice.application.service.query;

import com.umc.product.notice.application.port.in.query.GetNoticeFilterUseCase;
import com.umc.product.notice.application.port.in.query.dto.NoticeScopeInfo;
import com.umc.product.notice.application.port.in.query.dto.WritableNoticeScopeOption;
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

    @Override
    public List<NoticeScopeInfo> getAvailableFilters() {
        return null;
    }

    @Override
    public WritableNoticeScopeOption getWritableNoticeScope() {
        return null;
    }
}
