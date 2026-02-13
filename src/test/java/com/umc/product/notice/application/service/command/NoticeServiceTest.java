package com.umc.product.notice.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.umc.product.authorization.application.port.in.query.GetMemberRolesUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notice.dto.NoticeTargetInfo;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock LoadNoticePort loadNoticePort;
    @Mock SaveNoticePort saveNoticePort;
    @Mock SaveNoticeTargetPort saveNoticeTargetPort;
    @Mock LoadChallengerPort loadChallengerPort;
    @Mock GetChallengerUseCase getChallengerUseCase;
    @Mock GetMemberRolesUseCase getMemberRolesUseCase;
    @Mock GetNoticeTargetUseCase getNoticeTargetUseCase;
    @Mock ManageFcmUseCase manageFcmUseCase;
    @Mock ManageNoticeContentUseCase manageNoticeContentUseCase;
    @Mock GetMemberUseCase getMemberUseCase;
    @Mock GetChapterUseCase getChapterUseCase;

    @InjectMocks NoticeService sut;

    private static final Long MEMBER_ID = 1L;
    private static final Long CHALLENGER_ID = 10L;
    private static final Long GISU_ID = 7L;
    private static final Long NOTICE_ID = 100L;

    private ChallengerInfo createChallengerInfo() {
        return ChallengerInfo.builder()
                .challengerId(CHALLENGER_ID)
                .memberId(MEMBER_ID)
                .gisuId(GISU_ID)
                .part(ChallengerPart.SPRINGBOOT)
                .build();
    }

    private Notice createNotice() {
        Notice notice = Notice.create("테스트 공지", "테스트 내용", CHALLENGER_ID, false);
        ReflectionTestUtils.setField(notice, "id", NOTICE_ID);
        return notice;
    }

    private NoticeTargetInfo createTargetInfo() {
        return new NoticeTargetInfo(GISU_ID, null, null, List.of());
    }

    @Nested
    @DisplayName("createNotice")
    class CreateNoticeTest {

        @Test
        void 공지사항을_정상적으로_생성한다() {
            // given
            NoticeTargetInfo targetInfo = createTargetInfo();
            var command = new CreateNoticeCommand(MEMBER_ID, "공지 제목", "공지 내용", false, targetInfo);

            given(getChallengerUseCase.getByMemberIdAndGisuId(MEMBER_ID, GISU_ID))
                    .willReturn(createChallengerInfo());
            given(getMemberRolesUseCase.isCentralCore(MEMBER_ID)).willReturn(true);
            given(saveNoticePort.save(any(Notice.class))).willAnswer(inv -> {
                Notice n = inv.getArgument(0);
                ReflectionTestUtils.setField(n, "id", NOTICE_ID);
                return n;
            });

            // when
            Long result = sut.createNotice(command);

            // then
            assertThat(result).isEqualTo(NOTICE_ID);
            then(saveNoticePort).should().save(any(Notice.class));
            then(saveNoticeTargetPort).should().save(any());
        }

        @Test
        void 권한이_없으면_공지사항_생성에_실패한다() {
            // given
            NoticeTargetInfo targetInfo = createTargetInfo();
            var command = new CreateNoticeCommand(MEMBER_ID, "공지 제목", "공지 내용", false, targetInfo);

            given(getChallengerUseCase.getByMemberIdAndGisuId(MEMBER_ID, GISU_ID))
                    .willReturn(createChallengerInfo());
            given(getMemberRolesUseCase.isCentralCore(MEMBER_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sut.createNotice(command))
                    .isInstanceOf(NoticeDomainException.class);
            then(saveNoticePort).should(never()).save(any());
        }

        @Test
        void 알림_발송_설정된_공지를_생성하면_FCM_알림을_전송한다() {
            // given
            NoticeTargetInfo targetInfo = createTargetInfo();
            var command = new CreateNoticeCommand(MEMBER_ID, "공지 제목", "공지 내용", true, targetInfo);

            given(getChallengerUseCase.getByMemberIdAndGisuId(MEMBER_ID, GISU_ID))
                    .willReturn(createChallengerInfo());
            given(getMemberRolesUseCase.isCentralCore(MEMBER_ID)).willReturn(true);
            given(saveNoticePort.save(any(Notice.class))).willAnswer(inv -> {
                Notice n = inv.getArgument(0);
                ReflectionTestUtils.setField(n, "id", NOTICE_ID);
                return n;
            });

            // when
            sut.createNotice(command);

            // then
            then(manageFcmUseCase).should().sendMessageByTopic(any());
        }
    }

    @Nested
    @DisplayName("updateNoticeTitleOrContent")
    class UpdateNoticeTest {

        @Test
        void 공지사항_제목과_내용을_수정한다() {
            // given
            Notice notice = createNotice();
            NoticeTargetInfo targetInfo = createTargetInfo();
            var command = new UpdateNoticeCommand(MEMBER_ID, NOTICE_ID, "수정된 제목", "수정된 내용");

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            given(getNoticeTargetUseCase.findByNoticeId(NOTICE_ID)).willReturn(targetInfo);
            given(getChallengerUseCase.getActiveByMemberIdAndGisuId(MEMBER_ID, GISU_ID))
                    .willReturn(createChallengerInfo());

            // when
            sut.updateNoticeTitleOrContent(command);

            // then
            assertThat(notice.getTitle()).isEqualTo("수정된 제목");
            assertThat(notice.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        void 작성자가_아니면_수정에_실패한다() {
            // given
            Notice notice = createNotice();
            NoticeTargetInfo targetInfo = createTargetInfo();
            var command = new UpdateNoticeCommand(2L, NOTICE_ID, "수정된 제목", "수정된 내용");

            ChallengerInfo otherChallenger = ChallengerInfo.builder()
                    .challengerId(999L)
                    .memberId(2L)
                    .gisuId(GISU_ID)
                    .part(ChallengerPart.SPRINGBOOT)
                    .build();

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            given(getNoticeTargetUseCase.findByNoticeId(NOTICE_ID)).willReturn(targetInfo);
            given(getChallengerUseCase.getActiveByMemberIdAndGisuId(2L, GISU_ID))
                    .willReturn(otherChallenger);

            // when & then
            assertThatThrownBy(() -> sut.updateNoticeTitleOrContent(command))
                    .isInstanceOf(NoticeDomainException.class);
        }

        @Test
        void 존재하지_않는_공지사항_수정_시_예외가_발생한다() {
            // given
            var command = new UpdateNoticeCommand(MEMBER_ID, 999L, "수정된 제목", "수정된 내용");
            given(loadNoticePort.findNoticeById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.updateNoticeTitleOrContent(command))
                    .isInstanceOf(NoticeDomainException.class);
        }
    }

    @Nested
    @DisplayName("deleteNotice")
    class DeleteNoticeTest {

        @Test
        void 공지사항을_삭제한다() {
            // given
            Notice notice = createNotice();
            var command = new DeleteNoticeCommand(MEMBER_ID, NOTICE_ID);

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            // when
            sut.deleteNotice(command);

            // then
            then(manageNoticeContentUseCase).should().removeContentsByNoticeId(NOTICE_ID, MEMBER_ID);
            then(saveNoticePort).should().delete(notice);
        }

        @Test
        void 존재하지_않는_공지사항_삭제_시_예외가_발생한다() {
            // given
            var command = new DeleteNoticeCommand(MEMBER_ID, 999L);
            given(loadNoticePort.findNoticeById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.deleteNotice(command))
                    .isInstanceOf(NoticeDomainException.class);
        }
    }

    @Nested
    @DisplayName("remindNotice")
    class RemindNoticeTest {

        @Test
        void 공지사항_리마인드_알림을_전송한다() {
            // given
            Notice notice = createNotice();
            List<Long> targetIds = List.of(1L, 2L, 3L);
            var command = new SendNoticeReminderCommand(MEMBER_ID, NOTICE_ID, targetIds);

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            // when
            sut.remindNotice(command);

            // then
            then(manageFcmUseCase).should(times(3)).sendMessageByToken(any(NotificationCommand.class));
        }

        @Test
        void 존재하지_않는_공지사항_리마인드_시_예외가_발생한다() {
            // given
            var command = new SendNoticeReminderCommand(MEMBER_ID, 999L, List.of(1L));
            given(loadNoticePort.findNoticeById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.remindNotice(command))
                    .isInstanceOf(NoticeDomainException.class);
        }
    }
}
