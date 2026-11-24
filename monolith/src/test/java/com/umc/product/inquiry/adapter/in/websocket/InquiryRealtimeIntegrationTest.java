package com.umc.product.inquiry.adapter.in.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.product.global.security.ParsedAccessToken;
import com.umc.product.global.websocket.application.port.out.BroadcastPort;
import com.umc.product.global.websocket.application.service.StompSubscriptionAuthorizerRegistry;
import com.umc.product.inquiry.application.port.in.command.CloseInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.CreateInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.SendInquiryMessageUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.CloseInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.SendInquiryMessageCommand;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;
import com.umc.product.inquiry.domain.enums.InquiryTeam;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.InquiryFixture;
import com.umc.product.support.fixture.MemberFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("문의 실시간 구독과 전달")
class InquiryRealtimeIntegrationTest extends IntegrationTestSupport {

    private static final String AUTHOR_TOKEN = "author-token";
    private static final String PROBE = "PROBE";
    private static final long FRAME_TIMEOUT_MILLIS = 10_000;
    private static final long QUIET_MILLIS = 500;

    @LocalServerPort
    int port;

    @Autowired
    CreateInquiryUseCase createInquiryUseCase;
    @Autowired
    SendInquiryMessageUseCase sendInquiryMessageUseCase;
    @Autowired
    CloseInquiryUseCase closeInquiryUseCase;
    @Autowired
    StompSubscriptionAuthorizerRegistry subscriptionAuthorizerRegistry;
    @Autowired
    BroadcastPort broadcastPort;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    MemberFixture memberFixture;
    @Autowired
    InquiryFixture inquiryFixture;

    private Long authorId;
    private Long staffId;
    private Long strangerId;
    private Long categoryId;
    private WebSocketStompClient stompClient;
    private StompSession session;

    @BeforeEach
    void setUp() {
        authorId = memberFixture.일반("author").getId();
        staffId = memberFixture.일반("staff").getId();
        strangerId = memberFixture.일반("stranger").getId();
        memberFixture.시스템_역할_부여(staffId, MemberSystemRoleType.UMC_PRODUCT);
        categoryId = inquiryFixture.카테고리("UMC 서비스", 1).getId();
        when(jwtTokenProvider.parseAndValidateAccessToken(eq(AUTHOR_TOKEN)))
            .thenReturn(new ParsedAccessToken(authorId, List.of(), null));
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
    }

    @AfterEach
    void tearDown() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        stompClient.stop();
    }

    @Test
    @DisplayName("작성자와 운영진은 문의 주소를 구독할 수 있고, 제3자와 없는 문의는 거절한다")
    void 구독_권한() {
        // given
        Long inquiryId = 문의_등록();
        String topic = "/topic/inquiries/" + inquiryId;

        // when & then
        assertThat(subscriptionAuthorizerRegistry.isAuthorized(authorId, topic)).isTrue();
        assertThat(subscriptionAuthorizerRegistry.isAuthorized(staffId, topic)).isTrue();
        assertThat(subscriptionAuthorizerRegistry.isAuthorized(strangerId, topic)).isFalse();
        assertThat(subscriptionAuthorizerRegistry.isAuthorized(authorId, "/topic/inquiries/" + (inquiryId + 1000)))
            .isFalse();
    }

    @Test
    @DisplayName("운영진이 처음 답하면 구독자는 새 메시지와 진행 중 상태를 받는다")
    void 운영진_첫_답변() throws Exception {
        // given
        Long inquiryId = 문의_등록();
        BlockingQueue<JsonNode> frames = 구독(inquiryId);

        // when
        InquiryMessageInfo reply = 보내기(inquiryId, staffId, "확인해 볼게요", UUID.randomUUID());

        // then
        List<JsonNode> received = 받기(frames, 2);
        JsonNode message = 종류(received, "MESSAGE_CREATED");
        assertThat(message.get("inquiryId").asLong()).isEqualTo(inquiryId);
        assertThat(message.at("/message/messageId").asLong()).isEqualTo(reply.messageId());
        assertThat(message.at("/message/senderType").asText()).isEqualTo("STAFF");
        assertThat(message.at("/message/senderMemberId").asLong()).isEqualTo(staffId);
        assertThat(message.at("/message/content").asText()).isEqualTo("확인해 볼게요");
        assertThat(message.at("/message/attachments").isArray()).isTrue();
        assertThat(message.at("/message/createdAt").asText()).isEqualTo(reply.createdAt().toString());

        JsonNode status = 종류(received, "STATUS_CHANGED");
        assertThat(status.get("inquiryId").asLong()).isEqualTo(inquiryId);
        assertThat(status.get("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(status.get("closedAt").isNull()).isTrue();
        assertThat(status.get("lastMessageAt").asText()).isEqualTo(reply.createdAt().toString());
    }

    @Test
    @DisplayName("작성자 메시지는 상태를 바꾸지 않으므로 새 메시지만 보낸다")
    void 작성자_메시지() throws Exception {
        // given
        Long inquiryId = 문의_등록();
        BlockingQueue<JsonNode> frames = 구독(inquiryId);

        // when
        보내기(inquiryId, authorId, "추가로 스크린샷도 보내요", UUID.randomUUID());

        // then
        assertThat(받기(frames, 1)).extracting(frame -> frame.get("type").asText())
            .containsExactly("MESSAGE_CREATED");
    }

    @Test
    @DisplayName("종료하면 종료 상태를, 종료 뒤 메시지가 오면 다시 진행 중 상태를 보낸다")
    void 종료와_재오픈() throws Exception {
        // given
        Long inquiryId = 문의_등록();
        BlockingQueue<JsonNode> frames = 구독(inquiryId);

        // when
        closeInquiryUseCase.close(new CloseInquiryCommand(inquiryId, staffId));

        // then
        JsonNode closed = 종류(받기(frames, 1), "STATUS_CHANGED");
        assertThat(closed.get("status").asText()).isEqualTo("CLOSED");
        assertThat(closed.get("closedAt").isNull()).isFalse();

        // when
        보내기(inquiryId, authorId, "아직 해결이 안 됐어요", UUID.randomUUID());

        // then
        List<JsonNode> reopened = 받기(frames, 2);
        종류(reopened, "MESSAGE_CREATED");
        JsonNode status = 종류(reopened, "STATUS_CHANGED");
        assertThat(status.get("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(status.get("closedAt").isNull()).isTrue();
    }

    @Test
    @DisplayName("같은 clientMessageId로 다시 보내면 아무것도 보내지 않는다")
    void 재전송() throws Exception {
        // given
        Long inquiryId = 문의_등록();
        UUID clientMessageId = UUID.randomUUID();
        BlockingQueue<JsonNode> frames = 구독(inquiryId);
        보내기(inquiryId, staffId, "확인해 볼게요", clientMessageId);
        받기(frames, 2);

        // when
        보내기(inquiryId, staffId, "확인해 볼게요", clientMessageId);

        // then
        assertThat(받기(frames, 0)).isEmpty();
    }

    private BlockingQueue<JsonNode> 구독(Long inquiryId) throws Exception {
        실시간_전달_대기();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + AUTHOR_TOKEN);
        session = stompClient.connectAsync(
            "ws://localhost:%d/ws/websocket".formatted(port),
            new WebSocketHttpHeaders(),
            connectHeaders,
            new StompSessionHandlerAdapter() {
            }
        ).get(5, TimeUnit.SECONDS);

        String topic = "/topic/inquiries/" + inquiryId;
        BlockingQueue<JsonNode> frames = new LinkedBlockingQueue<>();
        session.subscribe(topic, frameHandler(frames));
        for (int attempt = 0; attempt < 40; attempt++) {
            broadcastPort.broadcast(topic, Map.of("type", PROBE));
            JsonNode frame = frames.poll(250, TimeUnit.MILLISECONDS);
            if (frame != null) {
                frames.removeIf(it -> PROBE.equals(it.get("type").asText()));
                return frames;
            }
        }
        throw new AssertionError("문의 주소 구독이 준비되지 않았습니다: " + topic);
    }

    private List<JsonNode> 받기(BlockingQueue<JsonNode> frames, int expected) throws InterruptedException {
        실시간_전달_대기();
        List<JsonNode> received = new ArrayList<>();
        long deadline = System.currentTimeMillis() + FRAME_TIMEOUT_MILLIS;
        while (received.size() < expected && System.currentTimeMillis() < deadline) {
            JsonNode frame = frames.poll(100, TimeUnit.MILLISECONDS);
            if (frame != null) {
                received.add(frame);
            }
        }
        JsonNode extra;
        while ((extra = frames.poll(QUIET_MILLIS, TimeUnit.MILLISECONDS)) != null) {
            received.add(extra);
        }
        assertThat(received).as("받은 이벤트 수").hasSize(expected);
        return received;
    }

    // 실시간 outbox 행이 모두 PUBLISHED면 relay가 broadcast를 마친 것이다.
    private void 실시간_전달_대기() throws InterruptedException {
        long deadline = System.currentTimeMillis() + FRAME_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            Integer pending = jdbcTemplate.queryForObject(
                "select count(*) from event_outbox where processing_group = 'REALTIME' and status <> 'PUBLISHED'",
                Integer.class
            );
            if (pending != null && pending == 0) {
                return;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("실시간 outbox 전달이 끝나지 않았습니다");
    }

    private JsonNode 종류(List<JsonNode> frames, String type) {
        return frames.stream()
            .filter(frame -> type.equals(frame.get("type").asText()))
            .findFirst()
            .orElseThrow(() -> new AssertionError(type + " 이벤트가 없습니다: " + frames));
    }

    private StompFrameHandler frameHandler(BlockingQueue<JsonNode> frames) {
        return new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    frames.offer(objectMapper.readTree(new String((byte[]) payload, StandardCharsets.UTF_8)));
                } catch (Exception exception) {
                    throw new IllegalStateException(exception);
                }
            }
        };
    }

    private Long 문의_등록() {
        return createInquiryUseCase.create(new CreateInquiryCommand(
            authorId,
            categoryId,
            InquiryTeam.PLATFORM,
            "앱 로그인이 안 돼요",
            "로그인 버튼을 눌러도 반응이 없어요",
            List.of(),
            UUID.randomUUID()
        ));
    }

    private InquiryMessageInfo 보내기(Long inquiryId, Long senderId, String content, UUID clientMessageId) {
        return sendInquiryMessageUseCase.send(
            new SendInquiryMessageCommand(inquiryId, senderId, content, List.of(), clientMessageId)
        );
    }
}
