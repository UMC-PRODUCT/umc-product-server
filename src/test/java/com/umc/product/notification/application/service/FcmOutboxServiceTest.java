package com.umc.product.notification.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import com.umc.product.notification.application.port.in.ProcessFcmOutboxUseCase;
import com.umc.product.notification.application.port.out.LoadFcmOutboxPort;
import com.umc.product.notification.domain.FcmOutbox;
import com.umc.product.notification.domain.FcmOutboxStatus;
import com.umc.product.organization.domain.Chapter;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.UseCaseTestSupport;
import com.umc.product.support.fixture.ChallengerFixture;
import com.umc.product.support.fixture.ChapterFixture;
import com.umc.product.support.fixture.FcmOutboxFixture;
import com.umc.product.support.fixture.FcmTokenFixture;
import com.umc.product.support.fixture.GisuFixture;
import com.umc.product.support.fixture.MemberFixture;
import com.umc.product.support.fixture.SchoolFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FcmOutboxServiceTest extends UseCaseTestSupport {

    @Autowired
    private ProcessFcmOutboxUseCase processFcmOutboxUseCase;

    @Autowired
    private LoadFcmOutboxPort loadFcmOutboxPort;

    @Autowired
    private MemberFixture memberFixture;

    @Autowired
    private GisuFixture gisuFixture;

    @Autowired
    private ChapterFixture chapterFixture;

    @Autowired
    private SchoolFixture schoolFixture;

    @Autowired
    private ChallengerFixture challengerFixture;

    @Autowired
    private FcmTokenFixture fcmTokenFixture;

    @Autowired
    private FcmOutboxFixture fcmOutboxFixture;

    private Long memberId;

    @BeforeEach
    void setUp() throws Exception {
        Gisu gisu = gisuFixture.활성_기수(16L);
        Chapter chapter = chapterFixture.지부(gisu, "서울");
        School school = schoolFixture.지부에_소속된_학교("한국대", chapter);
        memberId = memberFixture.학교_소속_멤버("테스터", school.getId()).getId();
        challengerFixture.스프링_챌린저(memberId, gisu.getId());
        fcmTokenFixture.FCM_토큰(memberId, "test-token");

        TopicManagementResponse successResponse = mock(TopicManagementResponse.class);
        given(successResponse.getSuccessCount()).willReturn(1);
        given(successResponse.getFailureCount()).willReturn(0);
        given(firebaseMessaging.subscribeToTopic(anyList(), anyString())).willReturn(successResponse);
        given(firebaseMessaging.unsubscribeFromTopic(anyList(), anyString())).willReturn(successResponse);
    }

    @Test
    void SUBSCRIBE_이벤트_처리_성공_시_PROCESSED로_변경된다() {
        fcmOutboxFixture.구독_이벤트(memberId);

        processFcmOutboxUseCase.process();

        assertThat(loadFcmOutboxPort.findPendingEvents()).isEmpty();
    }

    @Test
    void UNSUBSCRIBE_이벤트_처리_성공_시_PROCESSED로_변경된다() {
        fcmOutboxFixture.구독해제_이벤트(memberId, "old-token");

        processFcmOutboxUseCase.process();

        assertThat(loadFcmOutboxPort.findPendingEvents()).isEmpty();
    }

    @Test
    void FCM_호출_실패_시_retryCount가_증가하고_PENDING_상태를_유지한다() throws Exception {
        fcmOutboxFixture.구독_이벤트(memberId);
        FirebaseMessagingException ex = mock(FirebaseMessagingException.class);
        given(firebaseMessaging.subscribeToTopic(anyList(), anyString())).willThrow(ex);

        processFcmOutboxUseCase.process();

        List<FcmOutbox> pending = loadFcmOutboxPort.findPendingEvents();
        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getRetryCount()).isEqualTo(1);
        assertThat(pending.get(0).getStatus()).isEqualTo(FcmOutboxStatus.PENDING);
    }

    @Test
    void FCM_호출이_3회_연속_실패하면_FAILED로_변경된다() throws Exception {
        fcmOutboxFixture.구독_이벤트(memberId);
        FirebaseMessagingException ex = mock(FirebaseMessagingException.class);
        given(firebaseMessaging.subscribeToTopic(anyList(), anyString())).willThrow(ex);

        processFcmOutboxUseCase.process(); // retryCount = 1
        processFcmOutboxUseCase.process(); // retryCount = 2
        processFcmOutboxUseCase.process(); // retryCount = 3 → FAILED

        assertThat(loadFcmOutboxPort.findPendingEvents()).isEmpty();
    }
}
