package com.umc.product.notice.application.service.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeRead;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.dto.NoticeTargetInfo;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoticeReadServiceTest {

    @Mock LoadNoticePort loadNoticePort;
    @Mock LoadNoticeReadPort loadNoticeReadPort;
    @Mock SaveNoticeReadPort saveNoticeReadPort;
    @Mock GetChallengerUseCase getChallengerUseCase;
    @Mock GetNoticeTargetUseCase getNoticeTargetUseCase;

    @InjectMocks NoticeReadService sut;

    private static final Long NOTICE_ID = 100L;
    private static final Long MEMBER_ID = 1L;
    private static final Long CHALLENGER_ID = 10L;
    private static final Long GISU_ID = 7L;

    private Notice createNotice() {
        Notice notice = Notice.create("테스트 공지", "테스트 내용", 10L, false);
        ReflectionTestUtils.setField(notice, "id", NOTICE_ID);
        return notice;
    }

    private ChallengerInfo createChallengerInfo() {
        return ChallengerInfo.builder()
                .challengerId(CHALLENGER_ID)
                .memberId(MEMBER_ID)
                .gisuId(GISU_ID)
                .part(ChallengerPart.SPRINGBOOT)
                .build();
    }

    private NoticeTargetInfo createTargetInfo() {
        return new NoticeTargetInfo(GISU_ID, null, null, List.of());
    }

    @Nested
    @DisplayName("recordRead")
    class RecordReadTest {

        @Test
        void 공지사항_읽음을_기록한다() {
            // given
            Notice notice = createNotice();
            NoticeTargetInfo targetInfo = createTargetInfo();

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            given(getNoticeTargetUseCase.findByNoticeId(NOTICE_ID)).willReturn(targetInfo);
            given(getChallengerUseCase.getActiveByMemberIdAndGisuId(MEMBER_ID, GISU_ID))
                    .willReturn(createChallengerInfo());
            given(loadNoticeReadPort.existsRead(NOTICE_ID, CHALLENGER_ID)).willReturn(false);

            // when
            sut.recordRead(NOTICE_ID, MEMBER_ID);

            // then
            then(saveNoticeReadPort).should().saveRead(any(NoticeRead.class));
        }

        @Test
        void 이미_읽은_공지사항은_중복_저장하지_않는다() {
            // given
            Notice notice = createNotice();
            NoticeTargetInfo targetInfo = createTargetInfo();

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            given(getNoticeTargetUseCase.findByNoticeId(NOTICE_ID)).willReturn(targetInfo);
            given(getChallengerUseCase.getActiveByMemberIdAndGisuId(MEMBER_ID, GISU_ID))
                    .willReturn(createChallengerInfo());
            given(loadNoticeReadPort.existsRead(NOTICE_ID, CHALLENGER_ID)).willReturn(true);

            // when
            sut.recordRead(NOTICE_ID, MEMBER_ID);

            // then
            then(saveNoticeReadPort).should(never()).saveRead(any());
        }

        @Test
        void 존재하지_않는_공지사항_읽음_처리_시_예외가_발생한다() {
            // given
            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.empty());

            // when & then
            Assertions.assertThatThrownBy(() -> sut.recordRead(NOTICE_ID, MEMBER_ID))
                    .isInstanceOf(NoticeDomainException.class);
        }

        @Test
        void 전체_기수_공지_읽음_시_최근_챌린저로_기록한다() {
            // given
            Notice notice = createNotice();
            NoticeTargetInfo allGisuTarget = new NoticeTargetInfo(null, null, null, List.of());

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            given(getNoticeTargetUseCase.findByNoticeId(NOTICE_ID)).willReturn(allGisuTarget);
            given(getChallengerUseCase.getLatestActiveChallengerByMemberId(MEMBER_ID))
                    .willReturn(ChallengerInfoWithStatus.builder()
                            .challengerId(CHALLENGER_ID)
                            .memberId(MEMBER_ID)
                            .gisuId(GISU_ID)
                            .part(ChallengerPart.SPRINGBOOT)
                            .status(ChallengerStatus.ACTIVE)
                            .build());
            given(loadNoticeReadPort.existsRead(NOTICE_ID, CHALLENGER_ID)).willReturn(false);

            // when
            sut.recordRead(NOTICE_ID, MEMBER_ID);

            // then
            then(saveNoticeReadPort).should().saveRead(any(NoticeRead.class));
        }
    }
}
