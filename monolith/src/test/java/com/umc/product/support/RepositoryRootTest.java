package com.umc.product.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 멀티 모듈로 나뉜 뒤 테스트 작업 디렉터리가 바뀌어도 저장소 공용 자산을 찾을 수 있는지 지킨다.
 * 이 테스트가 깨지면 컨테이너 기반 테스트가 Dockerfile 을 못 찾는 상태라는 뜻이다.
 */
@DisplayName("RepositoryRoot")
class RepositoryRootTest {

    private static final String MARKER = "settings.gradle.kts";

    @Nested
    @DisplayName("설정된 루트가 있으면")
    class WhenRootConfigured {

        @Test
        @DisplayName("작업 디렉터리를 무시하고 설정값을 쓴다")
        void prefersConfiguredRootOverWorkingDirectory(@TempDir Path tempDir) throws IOException {
            Path configuredRoot = Files.createDirectories(tempDir.resolve("configured"));
            Path unrelatedWorkingDirectory = Files.createDirectories(tempDir.resolve("elsewhere"));

            Path located = RepositoryRoot.locate(configuredRoot.toString(), unrelatedWorkingDirectory);

            assertThat(located).isEqualTo(configuredRoot);
        }

        @Test
        @DisplayName("설정값에 marker 가 없어도 그대로 믿는다")
        void doesNotValidateConfiguredRoot(@TempDir Path tempDir) {
            Path located = RepositoryRoot.locate(tempDir.toString(), tempDir);

            assertThat(located).isEqualTo(tempDir);
        }
    }

    @Nested
    @DisplayName("설정된 루트가 없으면")
    class WhenRootMissing {

        @Test
        @DisplayName("하위 폴더에서 시작해도 위로 올라가 최상단을 찾는다")
        void searchesUpwardsFromNestedDirectory(@TempDir Path tempDir) throws IOException {
            Files.createFile(tempDir.resolve(MARKER));
            Path nested = Files.createDirectories(tempDir.resolve("monolith/src/test/java"));

            Path located = RepositoryRoot.locate(null, nested);

            assertThat(located).isEqualTo(tempDir);
        }

        @Test
        @DisplayName("빈 문자열도 설정되지 않은 것으로 본다")
        void treatsBlankAsMissing(@TempDir Path tempDir) throws IOException {
            Files.createFile(tempDir.resolve(MARKER));
            Path nested = Files.createDirectories(tempDir.resolve("blog"));

            Path located = RepositoryRoot.locate("   ", nested);

            assertThat(located).isEqualTo(tempDir);
        }

        @Test
        @DisplayName("시작 폴더 자신이 최상단이면 그대로 돌려준다")
        void returnsStartDirectoryWhenItHoldsMarker(@TempDir Path tempDir) throws IOException {
            Files.createFile(tempDir.resolve(MARKER));

            Path located = RepositoryRoot.locate(null, tempDir);

            assertThat(located).isEqualTo(tempDir);
        }

        @Test
        @DisplayName("끝까지 못 찾으면 원인을 알려주는 예외를 던진다")
        void failsWithDiagnosticMessage(@TempDir Path tempDir) throws IOException {
            Path nested = Files.createDirectories(tempDir.resolve("no/marker/anywhere"));

            assertThatThrownBy(() -> RepositoryRoot.locate(null, nested))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("umc.repository.root")
                .hasMessageContaining(MARKER)
                .hasMessageContaining(nested.toString());
        }
    }

    @Nested
    @DisplayName("실제 저장소에서")
    class InThisRepository {

        @Test
        @DisplayName("공용 자산의 실제 경로를 돌려준다")
        void resolvesSharedAsset() {
            Path dockerfile = RepositoryRoot.resolve("docker/test/postgis/Dockerfile");

            assertThat(dockerfile).isAbsolute();
            assertThat(dockerfile)
                .as("저장소 최상단 기준으로 %s 를 찾지 못했다", dockerfile)
                .exists();
        }

        @Test
        @DisplayName("최상단에는 settings.gradle.kts 가 있다")
        void repositoryRootHoldsMarker() {
            assertThat(RepositoryRoot.resolve(MARKER)).exists();
        }
    }
}
