package com.umc.product.notice.application.service.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.exception.AuthorizationDomainException;
import com.umc.product.authorization.domain.exception.AuthorizationErrorCode;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeTab;

@ExtendWith(MockitoExtension.class)
class NoticeReadServiceTest {

    @Mock private LoadNoticePort loadNoticePort;
    @Mock private LoadNoticeReadPort loadNoticeReadPort;
    @Mock private SaveNoticeReadPort saveNoticeReadPort;
    @Mock private GetChallengerUseCase getChallengerUseCase;
    @Mock private GetNoticeTargetUseCase getNoticeTargetUseCase;
    @Mock private CheckPermissionUseCase checkPermissionUseCase;

    @InjectMocks private NoticeReadService service;

    @Test
    void 대상_기수_이력이_없는_회원은_열람_권한을_확인한_후_본인_이력으로_기록한다() {
        공지와_이력_준비(이력(7L, 10L, ChallengerStatus.ACTIVE));
        when(getChallengerUseCase.getLatestActiveChallengerByMemberId(2L))
            .thenReturn(ChallengerInfoWithStatus.builder().challengerId(7L).build());

        service.recordRead(1L, 2L);

        verify(checkPermissionUseCase).checkOrThrow(2L,
            ResourcePermission.of(ResourceType.NOTICE, 1L, PermissionType.READ));
        verify(saveNoticeReadPort).recordReadIfAbsent(1L, 7L);
    }

    @Test
    void 열람_권한이_없으면_다른_기수_이력으로_읽음_처리할_수_없다() {
        공지와_이력_준비(이력(7L, 10L, ChallengerStatus.ACTIVE));
        doThrow(new AuthorizationDomainException(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED))
            .when(checkPermissionUseCase).checkOrThrow(2L,
                ResourcePermission.of(ResourceType.NOTICE, 1L, PermissionType.READ));

        assertThatThrownBy(() -> service.recordRead(1L, 2L)).isInstanceOf(AuthorizationDomainException.class);

        verify(getChallengerUseCase, never()).getLatestActiveChallengerByMemberId(2L);
        verifyNoInteractions(saveNoticeReadPort);
    }

    @Test
    void 수료생은_열람_권한이_있으면_대상_기수_챌린저로_읽음을_기록한다() {
        공지와_이력_준비(이력(7L, 11L, ChallengerStatus.GRADUATED), 이력(8L, 12L, ChallengerStatus.ACTIVE));

        service.recordRead(1L, 2L);

        verify(checkPermissionUseCase).checkOrThrow(2L,
            ResourcePermission.of(ResourceType.NOTICE, 1L, PermissionType.READ));
        verify(saveNoticeReadPort).recordReadIfAbsent(1L, 7L);
    }

    @Test
    void 공지_열람_권한이_없는_수료생은_읽음을_기록하지_못한다() {
        공지와_이력_준비(이력(7L, 11L, ChallengerStatus.GRADUATED));
        doThrow(new AuthorizationDomainException(AuthorizationErrorCode.RESOURCE_ACCESS_DENIED))
            .when(checkPermissionUseCase).checkOrThrow(2L,
                ResourcePermission.of(ResourceType.NOTICE, 1L, PermissionType.READ));

        assertThatThrownBy(() -> service.recordRead(1L, 2L)).isInstanceOf(AuthorizationDomainException.class);
        verifyNoInteractions(saveNoticeReadPort);
    }

    @ParameterizedTest
    @EnumSource(value = ChallengerStatus.class, names = {"WITHDRAWN", "EXPELLED"})
    void 탈부나_제명된_대상_기수_이력은_다른_이력으로_대체하지_않는다(ChallengerStatus status) {
        공지와_이력_준비(이력(7L, 11L, status), 이력(8L, 12L, ChallengerStatus.ACTIVE));

        assertThatThrownBy(() -> service.recordRead(1L, 2L)).isInstanceOf(ChallengerDomainException.class);

        verify(getChallengerUseCase, never()).getLatestActiveChallengerByMemberId(2L);
        verifyNoInteractions(saveNoticeReadPort);
    }

    @Test
    void 같은_회원의_다른_기수_이력으로_이미_읽었으면_중복_기록하지_않는다() {
        공지와_이력_준비(이력(7L, 10L, ChallengerStatus.ACTIVE), 이력(8L, 9L, ChallengerStatus.GRADUATED));
        when(getChallengerUseCase.getLatestActiveChallengerByMemberId(2L))
            .thenReturn(ChallengerInfoWithStatus.builder().challengerId(7L).build());
        when(loadNoticeReadPort.countReadsByChallengerIdIn(1L, List.of(7L, 8L))).thenReturn(1L);

        service.recordRead(1L, 2L);

        verifyNoInteractions(saveNoticeReadPort);
    }

    private void 공지와_이력_준비(ChallengerBasicInfo... history) {
        when(loadNoticePort.findNoticeById(1L)).thenReturn(Optional.of(Notice.create("제목", "내용", 3L, false, false)));
        when(getNoticeTargetUseCase.findByNoticeId(1L))
            .thenReturn(new NoticeTargetInfo(11L, null, null, List.of(), NoticeTab.SCHOOL_CORE));
        when(getChallengerUseCase.getAllBasicByMemberIds(Set.of(2L))).thenReturn(Map.of(2L, List.of(history)));
    }

    private static ChallengerBasicInfo 이력(Long challengerId, Long gisuId, ChallengerStatus status) {
        return new ChallengerBasicInfo(challengerId, 2L, gisuId, null, false, status);
    }
}
