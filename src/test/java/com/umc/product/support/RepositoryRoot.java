package com.umc.product.support;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 저장소 최상단 폴더를 찾아준다.
 *
 * <h2>왜 필요한가</h2>
 * 일부 테스트는 저장소에 있는 파일을 "현재 폴더 기준"으로 읽는다.
 *
 * <pre>{@code
 * Path.of("docker/test/postgis/Dockerfile")
 * Path.of("scripts/data/11th-curriculum-outline.json")
 * }</pre>
 *
 * 테스트를 돌릴 때의 현재 폴더는 <b>Gradle 프로젝트 폴더</b>다. 지금은 프로젝트가 하나뿐이라
 * 현재 폴더가 곧 저장소 최상단이어서 위 경로가 그대로 맞는다.
 *
 * <p>모듈을 나누면 현재 폴더가 {@code monolith/}, {@code blog/} 로 바뀐다. 그러면 위 코드는
 * {@code monolith/docker/test/postgis/Dockerfile} 을 찾게 되고, 그런 파일은 없다.
 *
 * <p>그래서 "저장소 최상단이 어디인지" 를 알려주는 공통 창구가 필요하다.
 *
 * <pre>{@code
 * RepositoryRoot.resolve("docker/test/postgis/Dockerfile")
 * }</pre>
 *
 * <h2>어떻게 찾는가</h2>
 * <ol>
 *   <li>Gradle 이 넘겨준 {@code umc.repository.root} 시스템 프로퍼티가 있으면 그 값을 쓴다.</li>
 *   <li>없으면 현재 폴더에서 위로 올라가며 {@code settings.gradle.kts} 를 찾는다.
 *       IDE 에서 테스트를 직접 실행하면 Gradle 이 끼어들지 않아 프로퍼티가 없는데,
 *       이 경우를 위한 대비다.</li>
 * </ol>
 *
 * <h2>이 클래스를 쓰지 말아야 하는 곳</h2>
 * 모듈 자신의 소스나 빌드 산출물 경로에는 쓰지 않는다. 아키텍처 테스트가 보는
 * {@code src/main/java} 나 {@code build/generated/querydsl} 은 <b>각 모듈 기준</b>이라야 맞으므로
 * 상대경로를 그대로 둔다.
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
     * 입력을 인자로 받아 탐색 규칙만 담당한다.
     *
     * <p>시스템 프로퍼티와 작업 디렉터리는 테스트에서 바꿀 수 없어서 (작업 디렉터리는 JVM 이
     * 변경을 지원하지 않고, 시스템 프로퍼티는 같은 키를 정적 초기화에서 읽는
     * {@link TestContainersConfig} 를 오염시킬 수 있다) 규칙만 떼어냈다.
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
