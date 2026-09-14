package com.umc.product.blog.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * blog 가 저장소의 다른 도메인에서 무엇을 가져다 쓰는지 고정한다.
 *
 * <h2>왜 필요한가</h2>
 * Gradle 은 {@code :monolith} 가 blog 를 못 보게 막아주지만, 반대 방향은 막지 못한다.
 * {@code :blog} 는 {@code :monolith} 전체를 의존하므로 아무 타입이나 끌어다 쓸 수 있다.
 * 계약 표면이 조용히 넓어지는 것을 막으려고 허용 목록을 둔다.
 *
 * <h2>이 테스트가 잡지 못하는 것</h2>
 * 일반 import 와 static import 만 본다. 아래는 검사 범위 밖이다.
 *
 * <ul>
 *   <li>같은 패키지 참조 (import 가 없다)</li>
 *   <li>완전한 이름을 코드에 그대로 쓴 참조</li>
 *   <li>허용된 타입이 뒤에 달고 오는 전이 의존</li>
 * </ul>
 *
 * 예를 들어 {@code MemberInfo} 가 새 타입을 참조해도 blog 의 import 가 그대로면 통과한다.
 * 즉 여기서 고정하는 것은 <b>직접 import 표면</b>이지 전체 의존이 아니다.
 */
@DisplayName("blog 모듈 경계")
class BlogModuleBoundaryTest {

    /** 모듈 기준 경로다. blog/src/main/java 가 아니다. Test 의 작업 디렉터리가 모듈 폴더이기 때문이다. */
    private static final Path MAIN_SOURCE_ROOT = Path.of("src/main/java");

    private static final String BLOG_PACKAGE_PREFIX = "com.umc.product.blog.";
    private static final String PRODUCT_PACKAGE_PREFIX = "com.umc.product.";

    /**
     * blog 가 외부에서 직접 import 해도 되는 타입.
     *
     * <p>여기에 줄을 추가한다는 것은 blog 의 외부 계약 표면을 넓힌다는 뜻이다.
     * 리뷰에서 그 판단을 하라고 목록을 명시해 둔다.
     */
    private static final Set<String> ALLOWED_EXTERNAL_TYPES = Set.of(
        "com.umc.product.audit.application.port.in.annotation.Audited",
        "com.umc.product.audit.domain.AuditAction",
        // 어댑터 계층이지만 횡단 관심사 애노테이션이라 예외로 둔다.
        "com.umc.product.authorization.adapter.in.aspect.CheckAccess",
        "com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase",
        "com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator",
        "com.umc.product.authorization.domain.PermissionType",
        "com.umc.product.authorization.domain.ResourcePermission",
        "com.umc.product.authorization.domain.ResourceType",
        "com.umc.product.authorization.domain.SubjectAttributes",
        "com.umc.product.common.BaseEntity",
        "com.umc.product.global.exception.BusinessException",
        "com.umc.product.global.exception.constant.Domain",
        "com.umc.product.global.response.CursorResponse",
        "com.umc.product.global.response.code.BaseCode",
        "com.umc.product.global.security.MemberPrincipal",
        "com.umc.product.global.security.annotation.CurrentMember",
        "com.umc.product.global.security.annotation.Public",
        "com.umc.product.member.application.port.in.query.GetMemberUseCase",
        "com.umc.product.member.application.port.in.query.dto.MemberInfo"
    );

    @Test
    @DisplayName("허용 목록에 없는 외부 타입을 import 하지 않는다")
    void doesNotImportUnlistedExternalTypes() {
        List<String> violations = new ArrayList<>();

        for (SourceFile source : sourceFiles()) {
            for (String imported : externalImportsOf(source)) {
                if (!ALLOWED_EXTERNAL_TYPES.contains(imported)) {
                    violations.add("%s -> %s".formatted(source.relativePath(), imported));
                }
            }
        }

        assertThat(violations)
            .as("blog 의 외부 계약 표면을 넓히려면 BlogModuleBoundaryTest 의 허용 목록을 명시적으로 갱신한다")
            .isEmpty();
    }

    @Test
    @DisplayName("다른 도메인의 영속성 어댑터와 서비스 구현을 import 하지 않는다")
    void doesNotReachIntoOtherDomainInternals() {
        List<String> violations = new ArrayList<>();

        for (SourceFile source : sourceFiles()) {
            for (String imported : externalImportsOf(source)) {
                if (imported.contains(".adapter.out.") || imported.contains(".application.service.")) {
                    violations.add("%s -> %s".formatted(source.relativePath(), imported));
                }
            }
        }

        assertThat(violations)
            .as("다른 도메인은 UseCase 를 통해서만 사용한다")
            .isEmpty();
    }

    @Test
    @DisplayName("검사 대상 소스를 실제로 읽는다")
    void actuallyScansSources() {
        assertThat(sourceFiles())
            .as("%s 에서 Java 파일을 찾지 못했다. 경로가 어긋나면 위 검사들이 조용히 통과한다",
                MAIN_SOURCE_ROOT.toAbsolutePath())
            .isNotEmpty();
    }

    /** blog 자신을 먼저 걸러낸다. blog 내부의 service, adapter.out 은 검사 대상이 아니다. */
    private Set<String> externalImportsOf(SourceFile source) {
        Set<String> imported = new LinkedHashSet<>();

        for (JavaImportScanner.ImportDeclaration declaration : JavaImportScanner.scan(source.content())) {
            String reference = declaration.reference();
            if (!reference.startsWith(PRODUCT_PACKAGE_PREFIX) || reference.startsWith(BLOG_PACKAGE_PREFIX)) {
                continue;
            }
            // 와일드카드는 무엇을 끌어오는지 알 수 없어 허용 목록으로 판단할 수 없다. 그대로 남겨 위반이 되게 둔다.
            if (declaration.isWildcard()) {
                imported.add(reference);
                continue;
            }
            imported.add(ALLOWED_EXTERNAL_TYPES.contains(reference)
                ? reference
                : declaration.withoutTrailingMember());
        }
        return imported;
    }

    private List<SourceFile> sourceFiles() {
        try (Stream<Path> paths = Files.walk(MAIN_SOURCE_ROOT)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .map(SourceFile::read)
                .toList();
        } catch (IOException e) {
            throw new IllegalStateException("blog 소스를 읽지 못했다: " + MAIN_SOURCE_ROOT.toAbsolutePath(), e);
        }
    }

    private record SourceFile(String relativePath, String content) {

        private static SourceFile read(Path path) {
            try {
                return new SourceFile(
                    MAIN_SOURCE_ROOT.relativize(path).toString(),
                    Files.readString(path, StandardCharsets.UTF_8)
                );
            } catch (IOException e) {
                throw new IllegalStateException("소스 파일을 읽지 못했다: " + path, e);
            }
        }
    }
}
