package com.umc.product.global.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoggingPlacementPolicyTest {

    private static final Path MAIN_SOURCE_ROOT = Path.of("src/main/java");
    private static final List<String> LOG_METHODS = List.of(
        "log.trace(", "log.debug(", "log.info(", "log.warn(", "log.error("
    );

    @Test
    @DisplayName("DTO와 도메인 enum은 운영 로그를 직접 남기지 않는다")
    void dto_and_domain_enum_do_not_log_directly() throws IOException {
        List<String> violations = findJavaFiles()
            .filter(LoggingPlacementPolicyTest::isDtoOrDomainEnum)
            .flatMap(this::findLoggerUsage)
            .toList();

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("운영 로그는 command 객체 전체를 직접 남기지 않는다")
    void logs_do_not_write_full_command_objects() throws IOException {
        List<String> violations = findJavaFiles()
            .flatMap(this::findFullCommandLogUsage)
            .toList();

        assertThat(violations).isEmpty();
    }

    private Stream<Path> findJavaFiles() throws IOException {
        return Files.walk(MAIN_SOURCE_ROOT)
            .filter(path -> path.toString().endsWith(".java"));
    }

    private static boolean isDtoOrDomainEnum(Path path) {
        String normalizedPath = path.toString();
        return normalizedPath.contains("/dto/") || normalizedPath.contains("/domain/enums/");
    }

    private Stream<String> findLoggerUsage(Path path) {
        return findLineViolations(path, line -> line.contains("@Slf4j") || usesLogger(line));
    }

    private Stream<String> findFullCommandLogUsage(Path path) {
        return findLineViolations(path, line ->
            usesLogger(line) && (line.contains("command={}") || line.contains("commands={}"))
        );
    }

    private static boolean usesLogger(String line) {
        return LOG_METHODS.stream().anyMatch(line::contains);
    }

    private Stream<String> findLineViolations(Path path, java.util.function.Predicate<String> predicate) {
        try {
            List<String> lines = Files.readAllLines(path);
            return IntStream.range(0, lines.size())
                .filter(index -> predicate.test(lines.get(index)))
                .mapToObj(index -> "%s:%d %s".formatted(path, index + 1, lines.get(index).trim()));
        } catch (IOException e) {
            throw new IllegalStateException("로그 위치 정책 테스트 파일 읽기 실패: " + path, e);
        }
    }
}
