package com.umc.product.notice.application.service.query;

import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.out.LoadMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.exception.MemberDomainException;
import com.umc.product.member.domain.exception.MemberErrorCode;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusResult;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.enums.NoticeClassification;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeQueryService implements GetNoticeUseCase {

    private final LoadNoticePort loadNoticePort;
    private final LoadNoticeReadPort loadNoticeReadPort;

    private final LoadChallengerPort loadChallengerPort;

    @Override
    public Page<NoticeSummary> getAllNoticeSummaries(NoticeClassification info, Pageable pageable) {
        Page<Notice> notices = loadNoticePort.findNoticesByClassification(info, pageable);
        // TODO: ChallengerPort 구현된 후 뒷 부분 구현 진행 (ChallengerRole 꺼내와야해서 port 구현 필요)
//        notices.map(
//                notice -> {
//                    Challenger challenger = loadChallengerPort.findById(notice.getAuthorChallengerId())
//                            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));
//
//                    NoticeSummary.from(notice, );
//                }
//        )

        throw new NoticeDomainException(NoticeErrorCode.NOT_IMPLEMENTED_YET);
    }

    @Override
    public Page<NoticeSummary> searchNoticesByKeyword(String keyword, Pageable pageable) {
        Page<Notice> notices = loadNoticePort.findNoticesByKeyword(keyword, pageable);
        // TODO: ChallengerPort 구현된 후 뒷 부분 구현 진행 (ChallengerRole 꺼내와야해서 port 구현 필요)


        throw new NoticeDomainException(NoticeErrorCode.NOT_IMPLEMENTED_YET);
    }

    @Override
    public NoticeInfo getNoticeDetail(Long noticeId) {
        Notice notice = findById(noticeId);
        // TODO: ChallengerPort 구현된 후 뒷 부분 구현 진행 (ChallengerRole 꺼내와야해서 port 구현 필요)


        throw new NoticeDomainException(NoticeErrorCode.NOT_IMPLEMENTED_YET);
    }

    @Override
    public NoticeReadStatusResult getReadStatus(GetNoticeStatusQuery command) {
        Notice notice = findById(command.noticeId());

        // TODO: 권한 작업하면서 진행 예정
        throw new NoticeDomainException(NoticeErrorCode.NOT_IMPLEMENTED_YET);
    }


    @Override
    public NoticeReadStatusSummary getReadStatistics(Long noticeId) {
        Notice notice = findById(noticeId);

        //TODO: 권한 작업하면서 진행 예정
        throw new NoticeDomainException(NoticeErrorCode.NOT_IMPLEMENTED_YET);
    }

    private Notice findById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId).orElseThrow(
                () -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }
}
