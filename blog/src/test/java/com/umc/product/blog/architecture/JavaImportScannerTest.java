package com.umc.product.blog.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 경계 검사가 기대는 추출 로직을 직접 검증한다.
 *
 * <p>현재 소스에서 통과하는 것과, 금지된 변경을 실제로 잡는 것은 별개다. 추출이 조용히 빠뜨리면
 * {@link BlogModuleBoundaryTest} 는 아무것도 못 막으면서 초록으로 남는다.
 */
@DisplayName("Java import 추출")
class JavaImportScannerTest {

    @Test
    @DisplayName("일반 import 를 읽는다")
    void readsPlainImport() {
        assertThat(JavaImportScanner.scan("import com.umc.product.member.domain.Member;"))
            .extracting("reference", "isStatic", "isWildcard")
            .containsExactly(tuple("com.umc.product.member.domain.Member", false, false));
    }

    @Test
    @DisplayName("앞에 공백이 있어도 읽는다")
    void readsIndentedImport() {
        assertThat(JavaImportScanner.scan("    import com.umc.product.member.domain.Member;"))
            .extracting("reference")
            .containsExactly("com.umc.product.member.domain.Member");
    }

    @Test
    @DisplayName("static import 를 구분해서 읽는다")
    void readsStaticImport() {
        assertThat(JavaImportScanner.scan("import static com.umc.product.member.domain.Member.of;"))
            .extracting("reference", "isStatic")
            .containsExactly(tuple("com.umc.product.member.domain.Member.of", true));
    }

    @Test
    @DisplayName("와일드카드 import 를 읽고 표시한다")
    void readsWildcardImport() {
        assertThat(JavaImportScanner.scan("import com.umc.product.member.domain.*;"))
            .extracting("reference", "isWildcard")
            .containsExactly(tuple("com.umc.product.member.domain.*", true));
    }

    @Test
    @DisplayName("static 와일드카드 import 를 읽는다")
    void readsStaticWildcardImport() {
        assertThat(JavaImportScanner.scan("import static com.umc.product.member.domain.Member.*;"))
            .extracting("reference", "isStatic", "isWildcard")
            .containsExactly(tuple("com.umc.product.member.domain.Member.*", true, true));
    }

    @Test
    @DisplayName("블록 주석 안의 import 는 읽지 않는다")
    void ignoresImportInsideBlockComment() {
        String source = """
            /*
            import com.umc.product.member.domain.Member;
            */
            import com.umc.product.audit.domain.AuditAction;
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }

    @Test
    @DisplayName("한 줄 주석 안의 import 는 읽지 않는다")
    void ignoresImportInsideLineComment() {
        assertThat(JavaImportScanner.scan("// import com.umc.product.member.domain.Member;")).isEmpty();
    }

    @Test
    @DisplayName("한 줄 주석 안의 /* 를 블록 주석 시작으로 오인하지 않는다")
    void doesNotTreatSlashStarInsideLineCommentAsBlockStart() {
        String source = """
            // 블록 주석은 /* 로 시작한다
            import com.umc.product.audit.domain.AuditAction;
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }

    @Test
    @DisplayName("블록 주석이 한 줄 주석보다 먼저 나오면 블록 주석으로 본다")
    void treatsBlockCommentBeforeLineComment() {
        String source = """
            /* 여기서 // 는 주석 안의 문자다
            import com.umc.product.member.domain.Member;
            */
            import com.umc.product.audit.domain.AuditAction;
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }

    @Test
    @DisplayName("javadoc 안의 import 는 읽지 않는다")
    void ignoresImportInsideJavadoc() {
        String source = """
            /**
             * import com.umc.product.member.domain.Member;
             */
            import com.umc.product.audit.domain.AuditAction;
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }

    @Test
    @DisplayName("타입 선언 뒤의 문자열은 읽지 않는다")
    void stopsAtTypeDeclaration() {
        String source = """
            import com.umc.product.audit.domain.AuditAction;

            class Sample {
                String code = "import com.umc.product.member.domain.Member;";
            }
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }

    @Test
    @DisplayName("import 와 타입 이름이 줄로 나뉘어도 읽는다")
    void readsImportSplitAcrossLines() {
        String source = """
            import
                com.umc.product.member.domain.Member;
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.member.domain.Member");
    }

    @Test
    @DisplayName("애너테이션이 선언과 같은 줄에 있어도 타입 시작으로 본다")
    void stopsAtAnnotatedTypeDeclarationOnSameLine() {
        String source = """
            import com.umc.product.audit.domain.AuditAction;

            @Deprecated public class Example {
                String code = \"""
            import com.umc.product.member.domain.Member;
            \""";
            }
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }

    @Test
    @DisplayName("애너테이션 인자 문자열에 닫는 괄호가 있어도 타입 시작으로 본다")
    void stopsAtTypeDeclarationWithParenthesisInsideAnnotationArgument() {
        String source = """
            import com.umc.product.audit.domain.AuditAction;

            @DisplayName("괄호 ) 가 있는 이름") public class Example {
                String code = \"""
            import com.umc.product.member.domain.Member;
            \""";
            }
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }

    @Test
    @DisplayName("import 중간에 블록 주석이 끼어도 읽는다")
    void readsImportWithInlineBlockComment() {
        assertThat(JavaImportScanner.scan("import/* 설명 */com.umc.product.member.domain.Member;"))
            .extracting("reference")
            .containsExactly("com.umc.product.member.domain.Member");
    }

    @Test
    @DisplayName("애너테이션 타입 선언도 타입 시작으로 본다")
    void stopsAtAnnotationTypeDeclaration() {
        String source = """
            import com.umc.product.audit.domain.AuditAction;

            public @interface Example {
            }
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }

    @Test
    @DisplayName("문자열 안의 주석 기호에 속지 않는다")
    void doesNotTreatCommentMarkersInsideStringLiteralAsComments() {
        String source = """
            import com.umc.product.audit.domain.AuditAction;

            @SuppressWarnings("//") public class Example {
                String example = \"""
            import com.umc.product.member.domain.Member;
            \""";
            }
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }

    @Test
    @DisplayName("문자 리터럴 안의 주석 기호에도 속지 않는다")
    void doesNotTreatCommentMarkersInsideCharLiteralAsComments() {
        String source = """
            import com.umc.product.audit.domain.AuditAction;

            class Example {
                char slash = '/';
            }
            """;

        assertThat(JavaImportScanner.scan(source))
            .extracting("reference")
            .containsExactly("com.umc.product.audit.domain.AuditAction");
    }
}
