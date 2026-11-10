package com.umc.product.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
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
    void 프록시_전달_헤더로_클라이언트_IP와_요청_주소를_판별한다() {
        ResponseEntity<Map<String, Object>> response = echo(proxyHeaders("198.51.100.7"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(seenByServer(response))
            .containsEntry("remoteAddr", "198.51.100.7")
            .containsEntry("remoteHost", "198.51.100.7")
            .containsEntry("scheme", "https")
            .containsEntry("secure", "true")
            .containsEntry("serverName", "api.example.test")
            .containsEntry("serverPort", "443")
            .containsEntry("xForwardedFor", null);
    }

    @Test
    @DisplayName("프록시가 넣지 않은 표준 전달 헤더는 클라이언트 IP와 요청 주소 판별에 쓰지 않는다")
    void 표준_전달_헤더는_판별에_쓰지_않는다() {
        HttpHeaders headers = proxyHeaders("198.51.100.8");
        headers.add("Forwarded", "for=203.0.113.9;host=evil.example;proto=http");

        ResponseEntity<Map<String, Object>> response = echo(headers);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(seenByServer(response))
            .containsEntry("remoteAddr", "198.51.100.8")
            .containsEntry("remoteHost", "198.51.100.8")
            .containsEntry("scheme", "https")
            .containsEntry("secure", "true")
            .containsEntry("serverName", "api.example.test")
            .containsEntry("serverPort", "443");
    }

    @Test
    @DisplayName("같은 공인 IP의 비로그인 요청은 다른 전달 헤더와 관계없이 한 제한 버킷을 나눠 쓴다")
    void 같은_공인_IP는_한_제한_버킷을_나눠_쓴다() {
        HttpHeaders plain = proxyHeaders("198.51.100.20");
        HttpHeaders forgedA = proxyHeaders("198.51.100.20");
        forgedA.add("Forwarded", "for=203.0.113.20");
        HttpHeaders forgedB = proxyHeaders("198.51.100.20");
        forgedB.add("Forwarded", "for=203.0.113.21");

        ResponseEntity<Map<String, Object>> first = echo(plain);
        ResponseEntity<Map<String, Object>> second = echo(forgedA);
        ResponseEntity<Map<String, Object>> third = echo(forgedB);
        ResponseEntity<Map<String, Object>> fourth = echo(plain);
        ResponseEntity<Map<String, Object>> otherClient = echo(proxyHeaders("198.51.100.21"));

        assertThat(remaining(first)).isEqualTo("2");
        assertThat(remaining(second)).isEqualTo("1");
        assertThat(remaining(third)).isEqualTo("0");
        assertThat(fourth.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(otherClient.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(remaining(otherClient)).isEqualTo("2");
    }

    private ResponseEntity<Map<String, Object>> echo(HttpHeaders headers) {
        return restTemplate.exchange(
            ECHO_PATH,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<>() {
            }
        );
    }

    private static HttpHeaders proxyHeaders(String clientIp) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", clientIp);
        headers.add("X-Forwarded-Proto", "https");
        headers.add("X-Forwarded-Host", "api.example.test");
        headers.add("X-Forwarded-Port", "443");
        return headers;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> seenByServer(ResponseEntity<Map<String, Object>> response) {
        return (Map<String, Object>) response.getBody().get("result");
    }

    private static String remaining(ResponseEntity<?> response) {
        return response.getHeaders().getFirst("X-RateLimit-Remaining");
    }

    @RestController
    static class RequestEchoController {

        @Public
        @GetMapping(ECHO_PATH)
        Map<String, Object> echo(HttpServletRequest request) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("remoteAddr", request.getRemoteAddr());
            body.put("remoteHost", request.getRemoteHost());
            body.put("scheme", request.getScheme());
            body.put("secure", String.valueOf(request.isSecure()));
            body.put("serverName", request.getServerName());
            body.put("serverPort", String.valueOf(request.getServerPort()));
            body.put("xForwardedFor", request.getHeader("X-Forwarded-For"));
            return body;
        }
    }
}
