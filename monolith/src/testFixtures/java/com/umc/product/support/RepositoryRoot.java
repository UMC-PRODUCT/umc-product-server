package com.umc.product.support;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 저장소 최상단 기준으로 공용 자산 경로를 해석한다.
 *
 * <p>테스트의 현재 폴더는 Gradle 프로젝트 폴더다. 멀티 모듈에서는 모듈 폴더가 되므로
 * {@code Path.of("docker/...")} 같은 저장소 기준 상대경로가 깨진다.
 *
 * <p>{@code umc.repository.root} 시스템 프로퍼티를 먼저 보고, 없으면 위로 올라가며
 * {@code settings.gradle.kts} 를 찾는다. 후자는 Gradle 을 거치지 않는 IDE 실행용이다.
 *
 * <p>모듈 자신의 소스나 빌드 산출물 경로에는 쓰지 않는다. 아키텍처 테스트가 보는
 * {@code src/main/java} 는 모듈 기준이라야 맞다.
 */
public final class RepositoryRoot {

    private static final String REPOSITORY_ROOT_PROPERTY = "umc.repository.root";
    private static final String ROOT_MARKER = "settings.gradle.kts";

    private RepositoryRoot() {
    }

    /**
     * 저장소 최상단 기준 상대경로를 실제 경로로 바꾼다.
     *
     * @param relativePath 저장소 최상단 기준 상대경로 (예: {@code "docker/test/postgis/Dockerfile"})
     */
    public static Path resolve(String relativePath) {
        return locate(System.getProperty(REPOSITORY_ROOT_PROPERTY), workingDirectory()).resolve(relativePath);
    }

    /**
     * 탐색 규칙만 담당한다. 작업 디렉터리는 JVM 이 변경을 지원하지 않고 시스템 프로퍼티는
     * {@link TestContainersConfig} 와 공유하므로, 전역 상태 없이 검증하려고 입력을 인자로 받는다.
     *
     * @param configuredRoot   {@code umc.repository.root} 값. 없으면 {@code null} 또는 빈 문자열
     * @param workingDirectory 탐색을 시작할 폴더
     */
    static Path locate(String configuredRoot, Path workingDirectory) {
        if (configuredRoot != null && !configuredRoot.isBlank()) {
            return Path.of(configuredRoot);
        }
        return searchUpwards(workingDirectory);
    }

    private static Path searchUpwards(Path workingDirectory) {
        Path directory = workingDirectory;
        while (directory != null) {
            if (Files.exists(directory.resolve(ROOT_MARKER))) {
                return directory;
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException(
            "저장소 최상단을 찾지 못했습니다. '%s' 시스템 프로퍼티가 없고, 시작 폴더(%s) 위쪽에 '%s' 도 없습니다."
                .formatted(REPOSITORY_ROOT_PROPERTY, workingDirectory, ROOT_MARKER)
        );
    }

    private static Path workingDirectory() {
        return Path.of("").toAbsolutePath();
    }
}
