package com.umc.product.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoggingUxWritingPolicyTest {

    private static final Path MAIN_SOURCE_ROOT = Path.of("src/main/java");
    private static final List<String> PASSIVE_AUDIT_PATTERNS = List.of(
        "되었습니다",
        "처리되었습니다",
        "완료되었습니다"
    );
    private static final List<String> ROUGH_LOG_PATTERNS = List.of(
        "오류 발생",
        "skip합니다",
        "스킵",
        "호출되었습니다",
        "완료:",
        "생성 완료",
        "전송 완료",
        "처리 완료",
        "성공:"
    );

    @Test
    @DisplayName("감사 로그 설명은 피동형 상태 보고 대신 행동 중심 문장으로 쓴다")
    void audit_descriptions_use_action_oriented_copy() throws IOException {
        List<String> violations = findJavaFiles()
            .flatMap(path -> findLineViolations(path, this::isPassiveAuditDescription))
            .toList();

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("운영 로그 문구는 거친 축약어와 상태 보고 표현을 피한다")
    void application_logs_follow_ux_writing_guidelines() throws IOException {
        List<String> violations = findJavaFiles()
            .flatMap(path -> findLineViolations(path, this::isRoughLogLine))
            .toList();

        assertThat(violations).isEmpty();
    }

    private Stream<Path> findJavaFiles() throws IOException {
        return Files.walk(MAIN_SOURCE_ROOT)
            .filter(path -> path.toString().endsWith(".java"));
    }

    private boolean isPassiveAuditDescription(String line) {
        return line.contains("description = \"'")
            && PASSIVE_AUDIT_PATTERNS.stream().anyMatch(line::contains);
    }

    private boolean isRoughLogLine(String line) {
        return line.contains("log.")
            && ROUGH_LOG_PATTERNS.stream().anyMatch(line::contains);
    }

    private Stream<String> findLineViolations(Path path, Predicate<String> predicate) {
        try {
            List<String> lines = Files.readAllLines(path);
            return IntStream.range(0, lines.size())
                .filter(index -> predicate.test(lines.get(index)))
                .mapToObj(index -> "%s:%d %s".formatted(path, index + 1, lines.get(index).trim()));
        } catch (IOException e) {
            throw new IllegalStateException("로그 UX 라이팅 정책 테스트 파일 읽기 실패: " + path, e);
        }
    }
}
