package com.umc.product.notice.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.notice.application.port.in.query.GetNoticeContentUseCase;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.LoadNoticeTargetPort;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoticeQueryServiceTest {

    @Mock LoadNoticePort loadNoticePort;
    @Mock LoadNoticeReadPort loadNoticeReadPort;
    @Mock LoadNoticeTargetPort loadNoticeTargetPort;
    @Mock GetChapterUseCase getChapterUseCase;
    @Mock GetGisuUseCase getGisuUseCase;
    @Mock GetMemberUseCase getMemberUseCase;
    @Mock GetChallengerUseCase getChallengerUseCase;
    @Mock GetNoticeContentUseCase getNoticeContentUseCase;

    @InjectMocks NoticeQueryService sut;

    private static final Long NOTICE_ID = 100L;

    private ChallengerInfo challenger(Long challengerId, Long memberId, Long gisuId) {
        return ChallengerInfo.builder()
            .challengerId(challengerId)
            .memberId(memberId)
            .gisuId(gisuId)
            .part(ChallengerPart.SPRINGBOOT)
            .build();
    }

    private MemberInfo memberInfo(Long memberId) {
        return MemberInfo.builder()
            .id(memberId)
            .name("name")
            .nickname("nickname")
            .build();
    }

    @Nested
    @DisplayName("getReadStatistics - 전체 기수 공지")
    class 전체_기수_읽음_통계 {

        @Test
        void 동일_멤버의_여러_챌린저는_최근_기수_하나만_대상으로_잡힌다() {
            // given
            NoticeTarget target = NoticeTarget.builder()
                .noticeId(NOTICE_ID)
                .targetGisuId(null)
                .build();

            given(loadNoticeTargetPort.findByNoticeId(NOTICE_ID)).willReturn(Optional.of(target));
            given(getGisuUseCase.getList()).willReturn(List.of(
                new GisuInfo(7L, 7L, 7L, Instant.now(), Instant.now(), false),
                new GisuInfo(8L, 8L, 8L, Instant.now(), Instant.now(), true)
            ));

            // 멤버1: 기수7, 기수8 둘 다 있음 → 기수8만 남아야 함
            // 멤버2: 기수7만 있음
            given(getChallengerUseCase.getByGisuId(7L)).willReturn(List.of(
                challenger(1L, 10L, 7L),
                challenger(2L, 20L, 7L)
            ));
            given(getChallengerUseCase.getByGisuId(8L)).willReturn(List.of(
                challenger(3L, 10L, 8L)
            ));

            // 멤버 프로필 조회
            given(getMemberUseCase.getProfiles(Set.of(10L, 20L))).willReturn(Map.of(
                10L, memberInfo(10L),
                20L, memberInfo(20L)
            ));

            given(loadNoticeReadPort.findNoticeReadByNoticeId(NOTICE_ID)).willReturn(List.of());

            // when
            NoticeReadStatusSummary result = sut.getReadStatistics(NOTICE_ID);

            // then — 멤버 2명이므로 대상자 2명
            assertThat(result.totalCount()).isEqualTo(2);
            assertThat(result.readCount()).isEqualTo(0);
            assertThat(result.unreadCount()).isEqualTo(2);
        }

        @Test
        void 특정_기수_공지는_해당_기수_챌린저를_그대로_대상으로_잡는다() {
            // given
            NoticeTarget target = NoticeTarget.builder()
                .noticeId(NOTICE_ID)
                .targetGisuId(8L)
                .build();

            given(loadNoticeTargetPort.findByNoticeId(NOTICE_ID)).willReturn(Optional.of(target));
            given(getChallengerUseCase.getByGisuId(8L)).willReturn(List.of(
                challenger(3L, 10L, 8L),
                challenger(4L, 20L, 8L),
                challenger(5L, 30L, 8L)
            ));

            given(getMemberUseCase.getProfiles(Set.of(10L, 20L, 30L))).willReturn(Map.of(
                10L, memberInfo(10L),
                20L, memberInfo(20L),
                30L, memberInfo(30L)
            ));

            given(loadNoticeReadPort.findNoticeReadByNoticeId(NOTICE_ID)).willReturn(List.of());

            // when
            NoticeReadStatusSummary result = sut.getReadStatistics(NOTICE_ID);

            // then
            assertThat(result.totalCount()).isEqualTo(3);
        }
    }
}
