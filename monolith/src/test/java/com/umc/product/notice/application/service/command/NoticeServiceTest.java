package com.umc.product.notice.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.ManageNoticeTargetPort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.application.service.query.NoticeAudienceResolver;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoticeService - 공지 생성 및 재알림")
class NoticeServiceTest {

    private static final Long AUTHOR_MEMBER_ID = 100L;
    private static final Long GISU_ID = 9L;

    @Mock
    LoadNoticePort loadNoticePort;
    @Mock
    SaveNoticePort saveNoticePort;
    @Mock
    SaveNoticeTargetPort saveNoticeTargetPort;
    @Mock
    ManageNoticeTargetPort manageNoticeTargetPort;
    @Mock
    SaveNoticeReadPort saveNoticeReadPort;
    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    ManageNoticeContentUseCase manageNoticeContentUseCase;
    @Mock
    RequestFcmNotificationUseCase requestFcmNotificationUseCase;
    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    NoticeAudienceResolver noticeAudienceResolver;

    @InjectMocks
    NoticeService sut;

    @Test
    @DisplayName("재알림에 공지 상세 딥링크와 변환된 수신자를 포함한다")
    void 재알림_딥링크_포함() {
        // Given
        Notice notice = Notice.create("제목", "내용", AUTHOR_MEMBER_ID, false, false);
        ReflectionTestUtils.setField(notice, "id", 42L);
        when(loadNoticePort.findNoticeById(42L)).thenReturn(Optional.of(notice));
        when(getChallengerUseCase.getAllByIds(Set.of(7L)))
            .thenReturn(List.of(ChallengerInfo.builder().challengerId(7L).memberId(70L).build()));

        // When
        sut.remindNotice(new SendNoticeReminderCommand(AUTHOR_MEMBER_ID, 42L, List.of(7L)));

        // Then
        ArgumentCaptor<RequestFcmNotificationCommand> captor =
            ArgumentCaptor.forClass(RequestFcmNotificationCommand.class);
        verify(requestFcmNotificationUseCase).request(captor.capture());
        assertThat(captor.getValue().deepLink()).isEqualTo("umc://notice/42");
        assertThat(captor.getValue().memberIds()).containsExactly(70L);
    }

    @Test
    @DisplayName("새 공지 알림에 저장된 공지 ID의 상세 딥링크를 포함한다")
    void 생성_알림_딥링크_포함() {
        // Given
        Notice notice = Notice.create("제목", "내용", AUTHOR_MEMBER_ID, true, false);
        ReflectionTestUtils.setField(notice, "id", 42L);
        when(saveNoticePort.save(any(Notice.class))).thenReturn(notice);
        when(getChallengerRoleUseCase.isCentralMemberInGisu(AUTHOR_MEMBER_ID, GISU_ID)).thenReturn(true);
        when(getGisuUseCase.getActiveGisuId()).thenReturn(GISU_ID);

        // When
        sut.createNotice(new CreateNoticeCommand(AUTHOR_MEMBER_ID, "제목", "내용", true, false,
            new NoticeTargetInfo(GISU_ID, null, null, List.of(), NoticeTab.CHALLENGER)));

        // Then
        ArgumentCaptor<RequestFcmNotificationCommand> captor =
            ArgumentCaptor.forClass(RequestFcmNotificationCommand.class);
        verify(requestFcmNotificationUseCase).request(captor.capture());
        assertThat(captor.getValue().deepLink()).isEqualTo("umc://notice/42");
    }

    @ParameterizedTest
    @MethodSource("알림_확장_대상")
    @DisplayName("운영진·전체 기수 공지도 계산한 수신자에게 알림을 요청한다")
    void 확장_대상_알림_요청(NoticeTargetInfo targetInfo) {
        // Given
        when(getChallengerRoleUseCase.isSuperAdmin(AUTHOR_MEMBER_ID)).thenReturn(true);
        Notice notice = Notice.create("제목", "내용", AUTHOR_MEMBER_ID, true, false);
        ReflectionTestUtils.setField(notice, "id", 42L);
        when(saveNoticePort.save(any(Notice.class))).thenReturn(notice);
        when(noticeAudienceResolver.resolve(targetInfo)).thenReturn(List.of(70L));
        CreateNoticeCommand command = new CreateNoticeCommand(
            AUTHOR_MEMBER_ID, "제목", "내용", true, false, targetInfo);

        // When
        assertThat(sut.createNotice(command)).isEqualTo(42L);

        // Then
        ArgumentCaptor<RequestFcmNotificationCommand> captor =
            ArgumentCaptor.forClass(RequestFcmNotificationCommand.class);
        verify(requestFcmNotificationUseCase).request(captor.capture());
        assertThat(captor.getValue().memberIds()).containsExactly(70L);
        assertThat(captor.getValue().targetGisuId()).isNull();
        assertThat(captor.getValue().targetSchoolId()).isNull();
        assertThat(captor.getValue().targetParts()).isEmpty();
        verifyNoInteractions(getGisuUseCase);
    }

    @ParameterizedTest
    @MethodSource("알림_확장_대상")
    @DisplayName("운영진·전체 기수 공지도 알림을 끄면 공지와 대상을 저장한다")
    void 제한_대상_알림_없이_게시(NoticeTargetInfo targetInfo) {
        // Given
        when(getChallengerRoleUseCase.isSuperAdmin(AUTHOR_MEMBER_ID)).thenReturn(true);
        Notice notice = Notice.create("제목", "내용", AUTHOR_MEMBER_ID, false, false);
        ReflectionTestUtils.setField(notice, "id", 42L);
        when(saveNoticePort.save(any(Notice.class))).thenReturn(notice);

        // When
        Long noticeId = sut.createNotice(new CreateNoticeCommand(
            AUTHOR_MEMBER_ID, "제목", "내용", false, false, targetInfo));

        // Then
        assertThat(noticeId).isEqualTo(42L);
        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        verify(saveNoticePort).save(captor.capture());
        assertThat(captor.getValue().isShouldSendNotification()).isFalse();
        verify(saveNoticeTargetPort).save(any());
        verifyNoInteractions(requestFcmNotificationUseCase);
        verifyNoInteractions(getGisuUseCase);
    }

    @ParameterizedTest
    @MethodSource("잘못된_대상_조합")
    @DisplayName("잘못된 대상 조합은 관리자 권한을 평가하기 전에 거부한다")
    void 잘못된_대상은_관리자_권한으로도_우회하지_못한다(NoticeTargetInfo targetInfo) {
        // Given
        CreateNoticeCommand command = new CreateNoticeCommand(
            AUTHOR_MEMBER_ID, "제목", "내용", false, false, targetInfo);

        // When / Then
        assertThatThrownBy(() -> sut.createNotice(command))
            .isInstanceOf(NoticeDomainException.class)
            .extracting("baseCode")
            .isEqualTo(NoticeErrorCode.INVALID_TARGET_SETTING);
        verifyNoInteractions(getChallengerRoleUseCase, getGisuUseCase, saveNoticePort, saveNoticeTargetPort);
    }

    private static Stream<NoticeTargetInfo> 잘못된_대상_조합() {
        return Stream.of(
            new NoticeTargetInfo(null, 3L, null, List.of(), NoticeTab.CHALLENGER),
            new NoticeTargetInfo(null, null, 5L, List.of(ChallengerPart.WEB_PRODUCT_ENGINEER), NoticeTab.CHALLENGER),
            new NoticeTargetInfo(GISU_ID, 3L, 5L, List.of(), NoticeTab.CHALLENGER),
            new NoticeTargetInfo(null, null, null, List.of(), NoticeTab.SCHOOL_CORE),
            new NoticeTargetInfo(GISU_ID, 3L, null, List.of(), NoticeTab.SCHOOL_CORE),
            new NoticeTargetInfo(GISU_ID, null, 5L, List.of(), NoticeTab.CENTRAL_MEMBER),
            new NoticeTargetInfo(GISU_ID, null, null, List.of(ChallengerPart.WEB_PRODUCT_ENGINEER), NoticeTab.SCHOOL_CORE)
        );
    }

    private static Stream<Arguments> 알림_확장_대상() {
        return Stream.of(
            Arguments.of(new NoticeTargetInfo(GISU_ID, null, null, List.of(), NoticeTab.CENTRAL_MEMBER)),
            Arguments.of(new NoticeTargetInfo(GISU_ID, null, null, List.of(), NoticeTab.SCHOOL_CORE)),
            Arguments.of(new NoticeTargetInfo(GISU_ID, null, null, List.of(), NoticeTab.SCHOOL_PART_LEADER)),
            Arguments.of(new NoticeTargetInfo(null, null, null, List.of(), NoticeTab.CHALLENGER)),
            Arguments.of(new NoticeTargetInfo(null, null, 3L, List.of(), NoticeTab.CHALLENGER))
        );
    }

    private CreateNoticeCommand createCommand(List<ChallengerPart> targetParts) {
        return new CreateNoticeCommand(
            AUTHOR_MEMBER_ID, "제목", "내용", false, false,
            new NoticeTargetInfo(GISU_ID, null, null, targetParts, NoticeTab.CHALLENGER)
        );
    }

    @Test
    @DisplayName("레거시 파트를 대상으로 지정하면 INVALID_TARGET_PART로 거부한다")
    void 레거시_파트_대상_거부() {
        CreateNoticeCommand command = createCommand(List.of(ChallengerPart.SPRINGBOOT));

        assertThatThrownBy(() -> sut.createNotice(command))
            .isInstanceOf(NoticeDomainException.class)
            .extracting("baseCode")
            .isEqualTo(NoticeErrorCode.INVALID_TARGET_PART);

        // 검증 단계에서 즉시 차단되어 저장까지 도달하지 않는다.
        verifyNoInteractions(saveNoticePort);
    }

    @Test
    @DisplayName("선택 가능한 파트와 레거시 파트가 섞여 있어도 거부한다")
    void 레거시_파트_혼합_거부() {
        CreateNoticeCommand command =
            createCommand(List.of(ChallengerPart.WEB_PRODUCT_ENGINEER, ChallengerPart.SPRINGBOOT));

        assertThatThrownBy(() -> sut.createNotice(command))
            .isInstanceOf(NoticeDomainException.class)
            .extracting("baseCode")
            .isEqualTo(NoticeErrorCode.INVALID_TARGET_PART);

        verifyNoInteractions(saveNoticePort);
    }

    @Test
    @DisplayName("대상 파트가 비어 있으면(전체 대상) 파트 검증을 통과한다")
    void 대상_파트_없음_통과() {
        CreateNoticeCommand command = createCommand(List.of());

        // 파트 검증은 통과하고, 이후 권한 검증(NO_WRITE_PERMISSION)에서 걸린다 → INVALID_TARGET_PART가 아니어야 한다.
        assertThatThrownBy(() -> sut.createNotice(command))
            .isInstanceOf(NoticeDomainException.class)
            .extracting("baseCode")
            .isNotEqualTo(NoticeErrorCode.INVALID_TARGET_PART);
    }

    @Test
    @DisplayName("선택 가능한 파트만 지정하면 파트 검증을 통과한다")
    void 선택가능_파트_통과() {
        CreateNoticeCommand command = createCommand(List.of(ChallengerPart.WEB_PRODUCT_ENGINEER));

        // 파트 검증은 통과하고, 이후 권한 검증에서 걸린다 → INVALID_TARGET_PART가 아니어야 한다.
        assertThatThrownBy(() -> sut.createNotice(command))
            .isInstanceOf(NoticeDomainException.class)
            .extracting("baseCode")
            .isNotEqualTo(NoticeErrorCode.INVALID_TARGET_PART);
    }
}
