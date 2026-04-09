package com.umc.product.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.yaml.snakeyaml.Yaml;

/**
 * Gmail SMTP Rate Limit 재현 테스트.
 *
 * <p>실제 Gmail SMTP 서버에 연결하여 동시/연속 전송 시 rate limit이 걸리는 지점을 확인한다.
 * SMTP 설정은 test/resources/application.yml의 spring.mail 설정을 읽는다.
 *
 * <p>주의: 실제 이메일이 전송됨.
 */
@Disabled
class SmtpRateLimitTest {

    private static JavaMailSenderImpl mailSender;
    private static String fromAddress;
    private static String toAddress;

    @SuppressWarnings("unchecked")
    @BeforeAll
    static void setUp() throws IOException {
        Yaml yaml = new Yaml();
        Map<String, Object> config;
        try (InputStream is = SmtpRateLimitTest.class.getClassLoader()
                .getResourceAsStream("application.yml")) {
            config = yaml.load(is);
        }

        Map<String, Object> spring = (Map<String, Object>) config.get("spring");
        Map<String, Object> mail = (Map<String, Object>) spring.get("mail");

        String host = (String) mail.get("host");
        int port = (int) mail.get("port");
        String username = (String) mail.get("username");
        String password = (String) mail.get("password");
        toAddress = (String) mail.get("to");

        mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        fromAddress = username;

        System.out.println("=== SMTP 설정 완료 ===");
        System.out.println("Host: " + host + ":" + port);
        System.out.println("From: " + fromAddress);
        System.out.println("To: " + toAddress);
        System.out.println();
    }

    /**
     * 프로덕션 환경 재현 테스트.
     * AsyncConfig와 동일한 ThreadPoolTaskExecutor(core=2, max=5, queue=50)를 사용하여
     * 60건의 요청을 빠르게 제출한다.
     *
     * 프로덕션에서 실제로 발생하는 상황:
     * - 스레드 2개가 SMTP 전송 중 (core)
     * - 나머지는 큐(50)에 대기
     * - 큐가 차면 스레드 5개까지 확장
     * - 큐 50 + 스레드 5 초과 시 → RejectedExecutionException
     */
    @Test
    void 프로덕션_환경_재현_테스트() throws InterruptedException {
        int totalRequests = 300;
        System.out.println("=== 프로덕션 환경 재현 테스트 시작: " + totalRequests + "건 ===");
        System.out.println("ThreadPool: core=2, max=5, queue=50 (AsyncConfig 동일)");
        System.out.println();

        // 프로덕션 AsyncConfig와 동일한 스레드풀
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("email-test-");
        executor.initialize();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);
        Queue<String> errors = new ConcurrentLinkedQueue<>();
        CountDownLatch doneLatch = new CountDownLatch(totalRequests);

        Instant testStart = Instant.now();

        for (int i = 1; i <= totalRequests; i++) {
            final int index = i;
            try {
                executor.execute(() -> {
                    try {
                        Instant sendStart = Instant.now();
                        sendSimpleEmail(index, "프로덕션재현");
                        long elapsed = Duration.between(sendStart, Instant.now()).toMillis();

                        successCount.incrementAndGet();
                        System.out.printf("[성공] #%02d - %dms (스레드: %s)%n",
                                index, elapsed, Thread.currentThread().getName());
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        String errorMsg = extractErrorMessage(e);
                        errors.add(String.format("#%02d: %s", index, errorMsg));
                        System.out.printf("[실패] #%02d - %s%n", index, errorMsg);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            } catch (RejectedExecutionException e) {
                rejectedCount.incrementAndGet();
                doneLatch.countDown();
                System.out.printf("[거부] #%02d - 큐 초과로 요청 거부됨%n", index);
            }
        }

        doneLatch.await();
        executor.shutdown();

        long totalElapsed = Duration.between(testStart, Instant.now()).toMillis();

        System.out.println();
        System.out.println("=== 프로덕션 환경 재현 결과 ===");
        System.out.println("총 요청: " + totalRequests);
        System.out.println("성공: " + successCount.get());
        System.out.println("실패 (SMTP 에러): " + failCount.get());
        System.out.println("거부 (큐 초과): " + rejectedCount.get());
        System.out.println("소요 시간: " + totalElapsed + "ms");
        if (!errors.isEmpty()) {
            System.out.println("에러 목록:");
            errors.forEach(e -> System.out.println("  " + e));
        }
        System.out.println();
    }

    // --- 헬퍼 ---

    /**
     * 동시 전송 배치를 실행하고 결과를 반환한다.
     * ready/start/done 래치 패턴으로 모든 스레드가 동시에 전송을 시작하도록 보장한다.
     */
    private BatchResult runConcurrentBatch(int concurrency, String testType) throws InterruptedException {
        CountDownLatch readyLatch = new CountDownLatch(concurrency);
        // 모든 스레드를 동시에 출발시키기 위한 신호 래치
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(concurrency);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        Queue<String> errors = new ConcurrentLinkedQueue<>();

        Instant batchStart = Instant.now();

        try (ExecutorService executor = Executors.newFixedThreadPool(concurrency)) {
            for (int i = 0; i < concurrency; i++) {
                final int index = i + 1;
                executor.submit(() -> {
                    try {
                        readyLatch.countDown();
                        startLatch.await();

                        Instant sendStart = Instant.now();
                        sendSimpleEmail(index, testType);
                        long elapsed = Duration.between(sendStart, Instant.now()).toMillis();

                        successCount.incrementAndGet();
                        System.out.printf("[성공] #%02d - %dms%n", index, elapsed);
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                        String errorMsg = extractErrorMessage(e);
                        errors.add(String.format("#%02d: %s", index, errorMsg));
                        System.out.printf("[실패] #%02d - %s%n", index, errorMsg);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            readyLatch.await();
            startLatch.countDown();
            doneLatch.await();
        }

        long elapsedMs = Duration.between(batchStart, Instant.now()).toMillis();
        return new BatchResult(successCount.get(), failCount.get(), elapsedMs, errors);
    }

    private void sendSimpleEmail(int index, String testType) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(toAddress);
        helper.setSubject("[Rate Limit 테스트] " + testType + " #" + index);
        helper.setText("Rate limit 테스트 이메일입니다. (" + testType + " #" + index + ")", false);

        mailSender.send(message);
    }

    private static String extractErrorMessage(Exception e) {
        return e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
    }

    private record BatchResult(int successCount, int failCount, long elapsedMs, Queue<String> errors) {}
}
