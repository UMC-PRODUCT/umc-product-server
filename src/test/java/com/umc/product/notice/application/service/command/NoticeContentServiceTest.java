package com.umc.product.notice.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeImagesCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeLinksCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeVoteCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeVoteResult;
import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeImagesCommand;
import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeLinksCommand;
import com.umc.product.notice.application.port.out.LoadNoticeImagePort;
import com.umc.product.notice.application.port.out.LoadNoticeLinkPort;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.application.port.out.SaveNoticeImagePort;
import com.umc.product.notice.application.port.out.SaveNoticeLinkPort;
import com.umc.product.notice.application.port.out.SaveNoticeVotePort;
import com.umc.product.notice.application.service.NoticeAuthorValidator;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeImage;
import com.umc.product.notice.domain.NoticeLink;
import com.umc.product.notice.domain.NoticeVote;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.survey.application.port.in.command.CreateVoteUseCase;
import com.umc.product.survey.application.port.in.command.DeleteVoteUseCase;
import java.time.Instant;
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
class NoticeContentServiceTest {

    @Mock LoadNoticeVotePort loadNoticeVotePort;
    @Mock LoadNoticeLinkPort loadNoticeLinkPort;
    @Mock LoadNoticeImagePort loadNoticeImagePort;
    @Mock SaveNoticeVotePort saveNoticeVotePort;
    @Mock SaveNoticeImagePort saveNoticeImagePort;
    @Mock SaveNoticeLinkPort saveNoticeLinkPort;
    @Mock LoadNoticePort loadNoticePort;
    @Mock CreateVoteUseCase createVoteUseCase;
    @Mock DeleteVoteUseCase deleteVoteUseCase;
    @Mock NoticeAuthorValidator noticeAuthorValidator;

    @InjectMocks NoticeContentService sut;

    private static final Long NOTICE_ID = 100L;
    private static final Long AUTHOR_MEMBER_ID = 1L;
    private static final Long AUTHOR_CHALLENGER_ID = 10L;
    private static final Long OTHER_MEMBER_ID = 999L;

    private Notice createNotice() {
        Notice notice = Notice.create("테스트 공지", "테스트 내용", AUTHOR_CHALLENGER_ID, false);
        ReflectionTestUtils.setField(notice, "id", NOTICE_ID);
        return notice;
    }

    private void stubValidateAuthorFail(Notice notice, Long memberId) {
        willThrow(new NoticeDomainException(NoticeErrorCode.NOTICE_AUTHOR_MISMATCH))
            .given(noticeAuthorValidator).validate(notice, memberId);
    }

    @Nested
    @DisplayName("addImages")
    class AddImagesTest {

        @Test
        void 공지사항에_이미지를_추가한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeImagesCommand(List.of("img1", "img2"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            given(loadNoticeImagePort.countImageByNoticeId(NOTICE_ID)).willReturn(0);
            given(loadNoticeImagePort.findNextImageDisplayOrder(NOTICE_ID)).willReturn(0);
            given(saveNoticeImagePort.saveAllImages(any())).willAnswer(inv -> {
                List<NoticeImage> images = inv.getArgument(0);
                for (int i = 0; i < images.size(); i++) {
                    ReflectionTestUtils.setField(images.get(i), "id", (long) (i + 1));
                }
                return images;
            });

            // when
            List<Long> result = sut.addImages(command, NOTICE_ID, AUTHOR_MEMBER_ID);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(1L, 2L);
        }

        @Test
        void 이미지가_10개를_초과하면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeImagesCommand(List.of("img1", "img2", "img3"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            given(loadNoticeImagePort.countImageByNoticeId(NOTICE_ID)).willReturn(8);

            // when & then
            assertThatThrownBy(() -> sut.addImages(command, NOTICE_ID, AUTHOR_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }

        @Test
        void 이미지_ID_목록이_비어있으면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeImagesCommand(List.of());

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));


            // when & then
            assertThatThrownBy(() -> sut.addImages(command, NOTICE_ID, AUTHOR_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }

        @Test
        void 이미지_ID_목록이_null이면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeImagesCommand(null);

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));


            // when & then
            assertThatThrownBy(() -> sut.addImages(command, NOTICE_ID, AUTHOR_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }

        @Test
        void 기존_이미지와_합쳐서_정확히_10개면_성공한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeImagesCommand(List.of("img1", "img2"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            given(loadNoticeImagePort.countImageByNoticeId(NOTICE_ID)).willReturn(8);
            given(loadNoticeImagePort.findNextImageDisplayOrder(NOTICE_ID)).willReturn(8);
            given(saveNoticeImagePort.saveAllImages(any())).willAnswer(inv -> {
                List<NoticeImage> images = inv.getArgument(0);
                for (int i = 0; i < images.size(); i++) {
                    ReflectionTestUtils.setField(images.get(i), "id", (long) (i + 1));
                }
                return images;
            });

            // when
            List<Long> result = sut.addImages(command, NOTICE_ID, AUTHOR_MEMBER_ID);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        void 작성자가_아닌_사용자가_이미지를_추가하면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeImagesCommand(List.of("img1"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            stubValidateAuthorFail(notice, OTHER_MEMBER_ID);

            // when & then
            assertThatThrownBy(() -> sut.addImages(command, NOTICE_ID, OTHER_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }
    }

    @Nested
    @DisplayName("addLinks")
    class AddLinksTest {

        @Test
        void 공지사항에_링크를_추가한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeLinksCommand(List.of("https://example.com", "https://test.com"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            given(loadNoticeLinkPort.findNextLinkDisplayOrder(NOTICE_ID)).willReturn(0);
            given(saveNoticeLinkPort.saveAllLinks(any())).willAnswer(inv -> {
                List<NoticeLink> links = inv.getArgument(0);
                for (int i = 0; i < links.size(); i++) {
                    ReflectionTestUtils.setField(links.get(i), "id", (long) (i + 1));
                }
                return links;
            });

            // when
            List<Long> result = sut.addLinks(command, NOTICE_ID, AUTHOR_MEMBER_ID);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        void 링크_목록이_비어있으면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeLinksCommand(List.of());

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));


            // when & then
            assertThatThrownBy(() -> sut.addLinks(command, NOTICE_ID, AUTHOR_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }

        @Test
        void 작성자가_아닌_사용자가_링크를_추가하면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeLinksCommand(List.of("https://example.com"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            stubValidateAuthorFail(notice, OTHER_MEMBER_ID);

            // when & then
            assertThatThrownBy(() -> sut.addLinks(command, NOTICE_ID, OTHER_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }
    }

    @Nested
    @DisplayName("addVote")
    class AddVoteTest {

        @Test
        void 공지사항에_투표를_추가한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeVoteCommand(
                AUTHOR_MEMBER_ID, "점심 메뉴 투표", true, false,
                Instant.now(), Instant.now().plusSeconds(86400 * 2),
                List.of("한식", "중식", "일식")
            );

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            given(loadNoticeVotePort.existsVoteByNoticeId(NOTICE_ID)).willReturn(false);
            given(createVoteUseCase.create(any())).willReturn(10L);
            given(saveNoticeVotePort.saveVote(any())).willAnswer(inv -> {
                NoticeVote vote = inv.getArgument(0);
                ReflectionTestUtils.setField(vote, "id", 1L);
                return vote;
            });

            // when
            AddNoticeVoteResult result = sut.addVote(command, NOTICE_ID);

            // then
            assertThat(result.noticeVoteId()).isEqualTo(1L);
            assertThat(result.voteId()).isEqualTo(10L);
            then(createVoteUseCase).should().create(any());
            then(saveNoticeVotePort).should().saveVote(any(NoticeVote.class));
        }

        @Test
        void 이미_투표가_존재하면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeVoteCommand(
                AUTHOR_MEMBER_ID, "점심 메뉴 투표", true, false,
                Instant.now(), Instant.now().plusSeconds(86400 * 2),
                List.of("한식", "중식", "일식")
            );

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            given(loadNoticeVotePort.existsVoteByNoticeId(NOTICE_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> sut.addVote(command, NOTICE_ID))
                .isInstanceOf(NoticeDomainException.class);
            then(createVoteUseCase).should(never()).create(any());
        }

        @Test
        void 작성자가_아닌_사용자가_투표를_추가하면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new AddNoticeVoteCommand(
                OTHER_MEMBER_ID, "점심 메뉴 투표", true, false,
                Instant.now(), Instant.now().plusSeconds(86400 * 2),
                List.of("한식", "중식", "일식")
            );

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            stubValidateAuthorFail(notice, OTHER_MEMBER_ID);

            // when & then
            assertThatThrownBy(() -> sut.addVote(command, NOTICE_ID))
                .isInstanceOf(NoticeDomainException.class);
            then(createVoteUseCase).should(never()).create(any());
        }
    }

    @Nested
    @DisplayName("deleteVote")
    class DeleteVoteTest {

        @Test
        void 공지사항의_투표를_삭제한다() {
            // given
            Notice notice = createNotice();
            NoticeVote noticeVote = NoticeVote.create(10L, notice);
            ReflectionTestUtils.setField(noticeVote, "id", 1L);

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            given(loadNoticeVotePort.findVoteByNoticeId(NOTICE_ID))
                .willReturn(Optional.of(noticeVote));

            // when
            sut.deleteVote(NOTICE_ID, AUTHOR_MEMBER_ID);

            // then
            then(deleteVoteUseCase).should().delete(any());
            then(saveNoticeVotePort).should().deleteVote(noticeVote);
        }

        @Test
        void 투표가_없는_공지사항에서_삭제하면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));

            given(loadNoticeVotePort.findVoteByNoticeId(NOTICE_ID))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.deleteVote(NOTICE_ID, AUTHOR_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
            then(deleteVoteUseCase).should(never()).delete(any());
        }

        @Test
        void 작성자가_아닌_사용자가_투표를_삭제하면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            stubValidateAuthorFail(notice, OTHER_MEMBER_ID);

            // when & then
            assertThatThrownBy(() -> sut.deleteVote(NOTICE_ID, OTHER_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
            then(deleteVoteUseCase).should(never()).delete(any());
        }
    }

    @Nested
    @DisplayName("removeContentsByNoticeId")
    class RemoveContentsTest {

        @Test
        void 공지사항의_모든_콘텐츠를_삭제한다() {
            // given
            Notice notice = createNotice();
            NoticeVote noticeVote = NoticeVote.create(10L, notice);
            ReflectionTestUtils.setField(noticeVote, "id", 1L);

            given(loadNoticeVotePort.findVoteByNoticeId(NOTICE_ID))
                .willReturn(Optional.of(noticeVote));

            // when
            sut.removeContentsByNoticeId(NOTICE_ID, 1L);

            // then
            then(saveNoticeImagePort).should().deleteAllImagesByNoticeId(NOTICE_ID);
            then(saveNoticeLinkPort).should().deleteAllLinksByNoticeId(NOTICE_ID);
            then(deleteVoteUseCase).should().delete(any());
            then(saveNoticeVotePort).should().deleteAllVotesByNoticeId(NOTICE_ID);
        }

        @Test
        void 투표가_없는_공지사항의_콘텐츠를_삭제한다() {
            // given
            given(loadNoticeVotePort.findVoteByNoticeId(NOTICE_ID))
                .willReturn(Optional.empty());

            // when
            sut.removeContentsByNoticeId(NOTICE_ID, 1L);

            // then
            then(saveNoticeImagePort).should().deleteAllImagesByNoticeId(NOTICE_ID);
            then(saveNoticeLinkPort).should().deleteAllLinksByNoticeId(NOTICE_ID);
            then(deleteVoteUseCase).should(never()).delete(any());
            then(saveNoticeVotePort).should(never()).deleteAllVotesByNoticeId(anyLong());
        }
    }

    @Nested
    @DisplayName("replaceImages")
    class ReplaceImagesTest {

        @Test
        void 공지사항_이미지를_전체_교체한다() {
            // given
            Notice notice = createNotice();
            var command = new ReplaceNoticeImagesCommand(List.of("newImg1", "newImg2"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));


            // when
            sut.replaceImages(command, NOTICE_ID, AUTHOR_MEMBER_ID);

            // then
            then(saveNoticeImagePort).should().deleteAllImagesByNoticeId(NOTICE_ID);
            then(saveNoticeImagePort).should().saveAllImages(any());
        }

        @Test
        void 교체할_이미지_목록이_null이면_아무_작업도_하지_않는다() {
            // given
            var command = new ReplaceNoticeImagesCommand(null);

            // when
            sut.replaceImages(command, NOTICE_ID, AUTHOR_MEMBER_ID);

            // then
            then(saveNoticeImagePort).should(never()).deleteAllImagesByNoticeId(anyLong());
        }

        @Test
        void 교체할_이미지_목록이_빈_리스트면_기존_이미지만_삭제한다() {
            // given
            Notice notice = createNotice();
            var command = new ReplaceNoticeImagesCommand(List.of());

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));


            // when
            sut.replaceImages(command, NOTICE_ID, AUTHOR_MEMBER_ID);

            // then
            then(saveNoticeImagePort).should().deleteAllImagesByNoticeId(NOTICE_ID);
            then(saveNoticeImagePort).should(never()).saveAllImages(any());
        }

        @Test
        void 교체할_이미지가_10개를_초과하면_예외가_발생한다() {
            // given
            List<String> tooManyImages = List.of(
                "i1", "i2", "i3", "i4", "i5", "i6", "i7", "i8", "i9", "i10", "i11");
            var command = new ReplaceNoticeImagesCommand(tooManyImages);

            // when & then
            assertThatThrownBy(() -> sut.replaceImages(command, NOTICE_ID, AUTHOR_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }

        @Test
        void 작성자가_아닌_사용자가_이미지를_교체하면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new ReplaceNoticeImagesCommand(List.of("newImg1"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            stubValidateAuthorFail(notice, OTHER_MEMBER_ID);

            // when & then
            assertThatThrownBy(() -> sut.replaceImages(command, NOTICE_ID, OTHER_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }
    }

    @Nested
    @DisplayName("replaceLinks")
    class ReplaceLinksTest {

        @Test
        void 공지사항_링크를_전체_교체한다() {
            // given
            Notice notice = createNotice();
            var command = new ReplaceNoticeLinksCommand(List.of("https://new1.com", "https://new2.com"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));


            // when
            sut.replaceLinks(command, NOTICE_ID, AUTHOR_MEMBER_ID);

            // then
            then(saveNoticeLinkPort).should().deleteAllLinksByNoticeId(NOTICE_ID);
            then(saveNoticeLinkPort).should().saveAllLinks(any());
        }

        @Test
        void 교체할_링크_목록이_null이면_아무_작업도_하지_않는다() {
            // given
            var command = new ReplaceNoticeLinksCommand(null);

            // when
            sut.replaceLinks(command, NOTICE_ID, AUTHOR_MEMBER_ID);

            // then
            then(saveNoticeLinkPort).should(never()).deleteAllLinksByNoticeId(anyLong());
        }

        @Test
        void 작성자가_아닌_사용자가_링크를_교체하면_예외가_발생한다() {
            // given
            Notice notice = createNotice();
            var command = new ReplaceNoticeLinksCommand(List.of("https://new1.com"));

            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.of(notice));
            stubValidateAuthorFail(notice, OTHER_MEMBER_ID);

            // when & then
            assertThatThrownBy(() -> sut.replaceLinks(command, NOTICE_ID, OTHER_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }
    }

    @Nested
    @DisplayName("존재하지 않는 공지사항")
    class NotFoundTest {

        @Test
        void 존재하지_않는_공지사항에_이미지_추가_시_예외가_발생한다() {
            // given
            var command = new AddNoticeImagesCommand(List.of("img1"));
            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.addImages(command, NOTICE_ID, AUTHOR_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }

        @Test
        void 존재하지_않는_공지사항에_링크_추가_시_예외가_발생한다() {
            // given
            var command = new AddNoticeLinksCommand(List.of("https://test.com"));
            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.addLinks(command, NOTICE_ID, AUTHOR_MEMBER_ID))
                .isInstanceOf(NoticeDomainException.class);
        }

        @Test
        void 존재하지_않는_공지사항에_투표_추가_시_예외가_발생한다() {
            // given
            var command = new AddNoticeVoteCommand(
                AUTHOR_MEMBER_ID, "투표", true, false,
                Instant.now(), Instant.now().plusSeconds(86400 * 2),
                List.of("옵션1", "옵션2")
            );
            given(loadNoticePort.findNoticeById(NOTICE_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.addVote(command, NOTICE_ID))
                .isInstanceOf(NoticeDomainException.class);
        }
    }
} 브
