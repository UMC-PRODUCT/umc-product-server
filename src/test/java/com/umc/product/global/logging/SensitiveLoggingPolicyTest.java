package com.umc.product.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SensitiveLoggingPolicyTest {

    private static final Path MAIN_SOURCE_ROOT = Path.of("src/main/java");
    private static final List<String> SENSITIVE_RAW_FIELD_NAMES = List.of(
        "body={}",
        "email={}",
        "providerId={}",
        "sub={}",
        "signedUrl={}",
        "signature={}",
        "Signature: {}",
        "Full URL: {}",
        "Final URL: {}"
    );
    private static final String NOTIFICATION_PACKAGE_PATH = "com/umc/product/notification";
    private static final String STORAGE_PACKAGE_PATH = "com/umc/product/storage";

    @Test
    @DisplayName("운영 로그는 민감한 OAuth 식별자와 외부 API 응답 본문을 직접 남기지 않는다")
    void logs_do_not_write_sensitive_raw_fields() throws IOException {
        List<String> violations = findLogViolations();

        assertThat(violations).isEmpty();
    }

    private List<String> findLogViolations() throws IOException {
        try (var paths = Files.walk(MAIN_SOURCE_ROOT)) {
            return paths
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(this::findLogViolationsInFile)
                .toList();
        }
    }

    private java.util.stream.Stream<String> findLogViolationsInFile(Path path) {
        try {
            List<String> lines = Files.readAllLines(path);
            return java.util.stream.IntStream.range(0, lines.size())
                .filter(index -> isSensitiveLogLine(path, lines.get(index)))
                .mapToObj(index -> "%s:%d %s".formatted(path, index + 1, lines.get(index).trim()));
        } catch (IOException e) {
            throw new IllegalStateException("로그 정책 테스트 파일 읽기 실패: " + path, e);
        }
    }

    private boolean isSensitiveLogLine(Path path, String line) {
        if (!line.contains("log.")) {
            return false;
        }
        if (SENSITIVE_RAW_FIELD_NAMES.stream().anyMatch(line::contains)) {
            return true;
        }
        if (path.toString().contains(NOTIFICATION_PACKAGE_PATH)
            && (line.contains("to={}") || line.contains("title={}") || line.contains("content={}"))) {
            return true;
        }
        return path.toString().contains(STORAGE_PACKAGE_PATH) && line.contains("url={}");
    }
}
