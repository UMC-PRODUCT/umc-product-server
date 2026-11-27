package com.umc.product.notice.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.query.ListChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.common.domain.enums.MemberStatus;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberBasicInfo;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.LoadNoticeTargetPort;
import com.umc.product.notice.application.service.query.NoticeReadAudienceService.Reader;
import com.umc.product.notice.domain.NoticeRead;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;

@ExtendWith(MockitoExtension.class)
class NoticeReadAudienceServiceTest {

    @Mock
    private LoadNoticeTargetPort loadNoticeTargetPort;
    @Mock
    private LoadNoticeReadPort loadNoticeReadPort;
    @Mock
    private GetChallengerUseCase getChallengerUseCase;
    @Mock
    private GetMemberUseCase getMemberUseCase;
    @Mock
    private ListChallengerRoleUseCase listChallengerRoleUseCase;
    @Mock
    private ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;
    @Mock
    private GetChapterUseCase getChapterUseCase;
    @InjectMocks
    private NoticeReadAudienceService service;

    @Test
    @DisplayName("전체 기수 공지를 이전 기수에서 읽었으면 현재 기수 명단에서도 한 명의 읽은 사람으로 집계한다")
    void readAcrossGisus() {
        givenGlobalNotice();
        List<NoticeRead> reads = List.of(read(5L, 101L), read(9L, 102L));
        given(loadNoticeReadPort.findNoticeReadByNoticeId(20L)).willReturn(reads);

        List<Reader> readers = service.listReaders(20L);

        assertThat(readers).singleElement().satisfies(reader -> {
            assertThat(reader.challenger().challengerId()).isEqualTo(102L);
            assertThat(reader.readId()).isEqualTo(5L);
        });
        assertThat(service.listUnreadRecipientMemberIds(20L, Set.of(101L, 102L))).isEmpty();
    }

    @Test
    @DisplayName("재알림 대상은 미확인 명단에 표시되는 최신 기수의 챌린저 ID로만 선택할 수 있다")
    void onlyListedChallengerIdCanBeSelected() {
        givenGlobalNotice();

        assertThat(service.listUnreadRecipientMemberIds(20L, Set.of(101L))).isEmpty();
        assertThat(service.listUnreadRecipientMemberIds(20L, Set.of(102L))).containsExactly(1L);
    }

    private void givenGlobalNotice() {
        given(loadNoticeTargetPort.findByNoticeId(20L)).willReturn(Optional.of(NoticeTarget.builder()
            .noticeId(20L).targetNoticeTab(NoticeTab.CHALLENGER).build()));
        List<ChallengerBasicInfo> history = List.of(
            new ChallengerBasicInfo(102L, 1L, 2L, ChallengerPart.PLAN, false, ChallengerStatus.ACTIVE),
            new ChallengerBasicInfo(101L, 1L, 1L, ChallengerPart.DESIGN, false, ChallengerStatus.GRADUATED));
        given(getChallengerUseCase.getHistoriesForActiveOrGraduatedMembers(null, Set.of()))
            .willReturn(Map.of(1L, history));
        given(getMemberUseCase.findAllBasicByIds(Set.of(1L))).willReturn(
            Map.of(1L, new MemberBasicInfo(1L, "회원", "닉네임", null, MemberStatus.ACTIVE)));
    }

    private NoticeRead read(long readId, long challengerId) {
        NoticeRead read = mock(NoticeRead.class);
        given(read.getId()).willReturn(readId);
        given(read.getChallengerId()).willReturn(challengerId);
        return read;
    }
}
