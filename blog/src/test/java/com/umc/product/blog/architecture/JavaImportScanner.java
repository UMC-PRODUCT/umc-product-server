package com.umc.product.blog.architecture;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java 원문에서 import 선언만 뽑아낸다.
 *
 * <p>두 단계로 나눈다. 먼저 문자 단위로 상태를 따라가며 주석을 지우되 문자열, 문자, 텍스트 블록은
 * 건드리지 않는다. 그다음 첫 타입 선언 앞까지만 잘라 그 안에서 import 를 찾는다. import 는 타입
 * 선언보다 앞에만 올 수 있으므로 본문의 문자열을 의존으로 오인하지 않는다.
 *
 * <p>정규식을 원문 전체에 그냥 돌리면 주석과 문자열 안의 예제까지 실제 의존으로 잡힌다.
 */
final class JavaImportScanner {

    /** {@code \s} 는 줄바꿈도 포함한다. import 선언이 여러 줄에 걸쳐도 잡힌다. */
    private static final Pattern IMPORT_PATTERN =
        Pattern.compile("^[ \\t]*import\\s+(static\\s+)?([A-Za-z0-9_.]+(?:\\.\\*)?)\\s*;", Pattern.MULTILINE);

    /**
     * 선언 앞에 붙은 애너테이션을 떼어낸다. {@code @Deprecated public class} 같은 한 줄 선언 때문이다.
     *
     * <p>인자는 문자열 안에 {@code )} 가 있을 수 있어 정규식으로 짝을 맞추지 않고 괄호를 세어 건너뛴다.
     */
    private static final Pattern ANNOTATION_NAME_PATTERN = Pattern.compile("^\\s*@[A-Za-z0-9_.]+");

    private static final Pattern TYPE_DECLARATION_PATTERN =
        Pattern.compile("^\\s*(?:(?:public|protected|private|static|final|abstract|sealed|non-sealed|strictfp)\\s+)*"
            + "(?:class|interface|enum|record)\\s");

    private static final Pattern ANNOTATION_TYPE_DECLARATION_PATTERN =
        Pattern.compile("^\\s*(?:(?:public|abstract|static)\\s+)*@interface\\s");

    private JavaImportScanner() {
    }

    static List<ImportDeclaration> scan(String sourceText) {
        String importRegion = importRegionOf(stripComments(sourceText));

        List<ImportDeclaration> declarations = new ArrayList<>();
        Matcher matcher = IMPORT_PATTERN.matcher(importRegion);
        while (matcher.find()) {
            String reference = matcher.group(2);
            declarations.add(new ImportDeclaration(
                reference,
                matcher.group(1) != null,
                reference.endsWith(".*")
            ));
        }
        return declarations;
    }

    /**
     * 주석을 지우되 줄 구조는 유지한다. 뒤에서 줄 단위로 타입 선언을 찾아야 하기 때문이다.
     *
     * <p>문자열, 문자, 텍스트 블록 안의 {@code //} 와 {@code /*} 는 주석이 아니다. 문자 단위로 상태를
     * 따라가지 않으면 {@code @SuppressWarnings("//")} 같은 줄이 통째로 잘려 타입 선언을 놓친다.
     */
    private static String stripComments(String sourceText) {
        StringBuilder cleaned = new StringBuilder(sourceText.length());
        State state = State.CODE;
        int index = 0;

        while (index < sourceText.length()) {
            char current = sourceText.charAt(index);

            switch (state) {
                case CODE -> {
                    if (sourceText.startsWith("\"\"\"", index)) {
                        state = State.TEXT_BLOCK;
                        cleaned.append("\"\"\"");
                        index += 3;
                        continue;
                    }
                    if (sourceText.startsWith("//", index)) {
                        state = State.LINE_COMMENT;
                        index += 2;
                        continue;
                    }
                    if (sourceText.startsWith("/*", index)) {
                        state = State.BLOCK_COMMENT;
                        // 주석 자리에 공백을 남긴다. 그냥 이으면 import/* c */com.x; 가 importcom.x; 가 된다.
                        cleaned.append(' ');
                        index += 2;
                        continue;
                    }
                    if (current == '"') {
                        state = State.STRING;
                    } else if (current == '\'') {
                        state = State.CHAR;
                    }
                    cleaned.append(current);
                }
                case STRING, CHAR, TEXT_BLOCK -> {
                    cleaned.append(current);
                    if (current == '\\' && index + 1 < sourceText.length()) {
                        cleaned.append(sourceText.charAt(index + 1));
                        index += 2;
                        continue;
                    }
                    if (state == State.TEXT_BLOCK && sourceText.startsWith("\"\"\"", index)) {
                        cleaned.append("\"\"");
                        index += 3;
                        state = State.CODE;
                        continue;
                    }
                    if (state == State.STRING && current == '"') {
                        state = State.CODE;
                    } else if (state == State.CHAR && current == '\'') {
                        state = State.CODE;
                    }
                }
                case LINE_COMMENT -> {
                    if (current == '\n') {
                        state = State.CODE;
                        cleaned.append('\n');
                    }
                }
                case BLOCK_COMMENT -> {
                    if (sourceText.startsWith("*/", index)) {
                        state = State.CODE;
                        index += 2;
                        continue;
                    }
                    // 줄 구조는 유지해야 뒤에서 타입 선언 위치를 찾을 수 있다.
                    if (current == '\n') {
                        cleaned.append('\n');
                    }
                }
                default -> { }
            }
            index++;
        }
        return cleaned.toString();
    }

    private enum State { CODE, STRING, CHAR, TEXT_BLOCK, LINE_COMMENT, BLOCK_COMMENT }

    /**
     * 첫 타입 선언 앞까지만 남긴다. import 는 그 앞에만 올 수 있으므로,
     * 뒤쪽 텍스트 블록이나 문자열 안의 {@code import ...;} 를 잘못 읽지 않는다.
     */
    private static String importRegionOf(String cleanedSource) {
        int offset = 0;
        for (String line : cleanedSource.split("\n", -1)) {
            if (isTypeDeclaration(line)) {
                return cleanedSource.substring(0, offset);
            }
            offset += line.length() + 1;
        }
        return cleanedSource;
    }

    private static boolean isTypeDeclaration(String line) {
        if (ANNOTATION_TYPE_DECLARATION_PATTERN.matcher(line).find()) {
            return true;
        }
        return TYPE_DECLARATION_PATTERN.matcher(stripLeadingAnnotations(line)).find();
    }

    private static String stripLeadingAnnotations(String line) {
        String remaining = line;
        while (true) {
            Matcher matcher = ANNOTATION_NAME_PATTERN.matcher(remaining);
            if (!matcher.find()) {
                return remaining;
            }
            remaining = remaining.substring(matcher.end());
            remaining = skipAnnotationArguments(remaining);
        }
    }

    /** 문자열 리터럴 안의 괄호에 속지 않도록 따옴표 밖의 괄호만 센다. */
    private static String skipAnnotationArguments(String afterAnnotationName) {
        String remaining = afterAnnotationName.stripLeading();
        if (!remaining.startsWith("(")) {
            return remaining;
        }

        int depth = 0;
        boolean insideString = false;
        for (int i = 0; i < remaining.length(); i++) {
            char current = remaining.charAt(i);
            if (insideString) {
                if (current == '\\') {
                    i++;
                } else if (current == '"') {
                    insideString = false;
                }
                continue;
            }
            switch (current) {
                case '"' -> insideString = true;
                case '(' -> depth++;
                case ')' -> {
                    depth--;
                    if (depth == 0) {
                        return remaining.substring(i + 1).stripLeading();
                    }
                }
                default -> { }
            }
        }
        // 인자가 다음 줄까지 이어진다. 이 줄에서는 타입 선언을 판정할 수 없다.
        return "";
    }

    record ImportDeclaration(String reference, boolean isStatic, boolean isWildcard) {
    }
}
