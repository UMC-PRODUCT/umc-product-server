package com.umc.product.blog.architecture;

import static com.umc.product.blog.architecture.BlogModuleBoundaryTest.externalImportsOf;
import static com.umc.product.blog.architecture.BlogModuleBoundaryTest.isAllowed;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.blog.architecture.JavaImportScanner.ImportDeclaration;

/**
 * 허용 목록 판정 자체를 검증한다.
 *
 * <p>{@link BlogModuleBoundaryTest} 는 현재 blog 소스가 통과하는지만 본다. 통과한다는 사실과
 * 금지된 형태를 실제로 잡는다는 사실은 별개다. 판정이 조용히 느슨해지면 경계 검사는 초록으로 남으면서
 * 아무것도 막지 못한다.
 */
@DisplayName("blog 모듈 경계 판정")
class BlogModuleBoundaryPolicyTest {

    /** 허용 목록에 실제로 들어 있는 타입. */
    private static final String ALLOWED = "com.umc.product.authorization.domain.SubjectAttributes";

    /** 허용 타입의 중첩 타입. 목록에 없다. */
    private static final String NESTED = ALLOWED + ".GisuChallengerInfo";

    @Test
    @DisplayName("허용 목록의 타입을 일반 import 하면 통과한다")
    void allowsListedTypeByPlainImport() {
        assertThat(judge("import " + ALLOWED + ";")).isTrue();
    }

    @Test
    @DisplayName("허용 목록에 있어도 static import 면 거부한다")
    void rejectsStaticImportEvenWhenTypeIsListed() {
        assertThat(judge("import static " + ALLOWED + ";")).isFalse();
    }

    @Test
    @DisplayName("허용 목록에 있어도 와일드카드면 거부한다")
    void rejectsWildcardEvenWhenPackageHasListedTypes() {
        assertThat(judge("import com.umc.product.authorization.domain.*;")).isFalse();
    }

    @Test
    @DisplayName("중첩 타입은 바깥 타입이 허용돼도 거부한다")
    void rejectsNestedTypeOfAllowedOuterType() {
        assertThat(judge("import " + NESTED + ";")).isFalse();
    }

    @Test
    @DisplayName("중첩 타입을 static import 로 가져와도 거부한다")
    void rejectsStaticImportOfNestedType() {
        assertThat(judge("import static " + NESTED + ";")).isFalse();
    }

    @Test
    @DisplayName("중첩 타입을 허용 목록에 넣더라도 static import 는 여전히 거부한다")
    void staticImportStaysRejectedIndependentlyOfTheAllowList() {
        ImportDeclaration plain = declarationOf("import " + ALLOWED + ";");
        ImportDeclaration statical = declarationOf("import static " + ALLOWED + ";");

        // 같은 이름이지만 판정이 갈린다. 목록에 없어서가 아니라 static 이라서 거부한다.
        assertThat(plain.reference()).isEqualTo(statical.reference());
        assertThat(isAllowed(plain)).isTrue();
        assertThat(isAllowed(statical)).isFalse();
    }

    @Test
    @DisplayName("blog 자신의 import 는 검사 대상이 아니다")
    void ignoresBlogOwnImports() {
        String source = """
            import com.umc.product.blog.application.service.BlogContentCommandService;
            import com.umc.product.blog.adapter.out.persistence.BlogContentJpaRepository;
            import static com.umc.product.blog.domain.QBlogContent.blogContent;
            """;

        assertThat(externalImportsOf(source)).isEmpty();
    }

    @Test
    @DisplayName("프로젝트 밖 라이브러리는 검사 대상이 아니다")
    void ignoresThirdPartyImports() {
        String source = """
            import java.util.List;
            import org.springframework.stereotype.Service;
            """;

        assertThat(externalImportsOf(source)).isEmpty();
    }

    private boolean judge(String sourceText) {
        return isAllowed(declarationOf(sourceText));
    }

    private ImportDeclaration declarationOf(String sourceText) {
        List<ImportDeclaration> declarations = externalImportsOf(sourceText);
        assertThat(declarations).as("검사 대상 import 를 하나 기대한다: %s", sourceText).hasSize(1);
        return declarations.getFirst();
    }
}
