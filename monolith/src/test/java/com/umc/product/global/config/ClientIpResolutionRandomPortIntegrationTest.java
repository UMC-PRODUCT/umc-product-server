package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.umc.product.global.security.JwtTokenProvider;
import com.umc.product.global.security.annotation.Public;
import com.umc.product.storage.application.port.out.StoragePort;
import com.umc.product.support.TestContainersConfig;

import jakarta.servlet.http.HttpServletRequest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "app.api-rate-limit.anonymous-default.requests-per-second=3",
        "app.api-rate-limit.anonymous-default.requests-per-minute=3"
    }
)
@ActiveProfiles("test")
@Import({TestContainersConfig.class, ClientIpResolutionRandomPortIntegrationTest.RequestEchoController.class})
@Testcontainers
@DisplayName("실제 내장 서버의 클라이언트 IP 판별")
class ClientIpResolutionRandomPortIntegrationTest {

    private static final String ECHO_PATH = "/api/v1/test-support/request-echo";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JavaMailSender mailSender;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

    @MockitoBean
    private StoragePort storagePort;

    @Test
    @DisplayName("프록시가 넣은 X-Forwarded-* 값으로 클라이언트 IP와 요청 주소를 판별한다")
    void 프록시_전달_헤더로_클라이언트_IP와_요청_주소를_판별한다() throws JsonProcessingException {
        // given
        HttpHeaders headers = proxyHeaders("198.51.100.7");

        // when
        ResponseEntity<JsonNode> response = echo(headers);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(seenByServer(response)).isEqualTo(seenThroughProxy("198.51.100.7"));
    }

    @Test
    @DisplayName("프록시가 넣지 않은 표준 전달 헤더는 클라이언트 IP와 요청 주소 판별에 쓰지 않는다")
    void 표준_전달_헤더는_판별에_쓰지_않는다() throws JsonProcessingException {
        // given
        HttpHeaders headers = proxyHeaders("198.51.100.8");
        headers.add("Forwarded", "for=203.0.113.9;host=evil.example;proto=http");

        // when
        ResponseEntity<JsonNode> response = echo(headers);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(seenByServer(response)).isEqualTo(seenThroughProxy("198.51.100.8"));
    }

    @Test
    @DisplayName("같은 공인 IP의 비로그인 요청은 다른 전달 헤더와 관계없이 한 제한 버킷을 나눠 쓴다")
    void 같은_공인_IP는_한_제한_버킷을_나눠_쓴다() {
        // given
        HttpHeaders plain = proxyHeaders("198.51.100.20");
        HttpHeaders forgedA = proxyHeaders("198.51.100.20");
        forgedA.add("Forwarded", "for=203.0.113.20");
        HttpHeaders forgedB = proxyHeaders("198.51.100.20");
        forgedB.add("Forwarded", "for=203.0.113.21");

        // when
        ResponseEntity<JsonNode> first = echo(plain);
        ResponseEntity<JsonNode> second = echo(forgedA);
        ResponseEntity<JsonNode> third = echo(forgedB);
        ResponseEntity<JsonNode> fourth = echo(plain);
        ResponseEntity<JsonNode> otherClient = echo(proxyHeaders("198.51.100.21"));

        // then
        assertThat(remaining(first)).isEqualTo("2");
        assertThat(remaining(second)).isEqualTo("1");
        assertThat(remaining(third)).isEqualTo("0");
        assertThat(fourth.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(otherClient.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(remaining(otherClient)).isEqualTo("2");
    }

    private ResponseEntity<JsonNode> echo(HttpHeaders headers) {
        return restTemplate.exchange(ECHO_PATH, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
    }

    private SeenRequest seenByServer(ResponseEntity<JsonNode> response) throws JsonProcessingException {
        return objectMapper.treeToValue(response.getBody().path("result"), SeenRequest.class);
    }

    private static SeenRequest seenThroughProxy(String clientIp) {
        return new SeenRequest(clientIp, clientIp, "https", true, "api.example.test", 443, null);
    }

    private static HttpHeaders proxyHeaders(String clientIp) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", clientIp);
        headers.add("X-Forwarded-Proto", "https");
        headers.add("X-Forwarded-Host", "api.example.test");
        headers.add("X-Forwarded-Port", "443");
        return headers;
    }

    private static String remaining(ResponseEntity<?> response) {
        return response.getHeaders().getFirst("X-RateLimit-Remaining");
    }

    record SeenRequest(
        String remoteAddr,
        String remoteHost,
        String scheme,
        boolean secure,
        String serverName,
        int serverPort,
        String xForwardedFor
    ) {
    }

    @RestController
    static class RequestEchoController {

        @Public
        @GetMapping(ECHO_PATH)
        SeenRequest echo(HttpServletRequest request) {
            return new SeenRequest(
                request.getRemoteAddr(),
                request.getRemoteHost(),
                request.getScheme(),
                request.isSecure(),
                request.getServerName(),
                request.getServerPort(),
                request.getHeader("X-Forwarded-For")
            );
        }
    }
}
