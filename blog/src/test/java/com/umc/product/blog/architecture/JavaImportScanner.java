package com.umc.product.blog.architecture;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java 원문에서 import 선언만 뽑아낸다.
 *
 * <p>정규식을 원문 전체에 그냥 돌리면 주석 안의 예제까지 실제 의존으로 잡힌다. 줄 단위로 읽으면서
 * 블록 주석을 건너뛰고, 첫 타입 선언을 만나면 멈춘다. import 는 타입 선언보다 앞에만 올 수 있으므로
 * 그 뒤의 문자열 리터럴을 잘못 읽는 일도 없앤다.
 */
final class JavaImportScanner {

    private static final Pattern IMPORT_PATTERN =
        Pattern.compile("^\\s*import\\s+(static\\s+)?([A-Za-z0-9_.]+(?:\\.\\*)?)\\s*;");

    private static final Pattern TYPE_DECLARATION_PATTERN =
        Pattern.compile("^\\s*(?:(?:public|final|abstract|sealed|non-sealed|strictfp)\\s+)*"
            + "(?:class|interface|enum|record|@interface)\\s");

    private JavaImportScanner() {
    }

    static List<ImportDeclaration> scan(String sourceText) {
        List<ImportDeclaration> declarations = new ArrayList<>();
        boolean insideBlockComment = false;

        for (String rawLine : sourceText.split("\n", -1)) {
            String line = rawLine;

            if (insideBlockComment) {
                int end = line.indexOf("*/");
                if (end < 0) {
                    continue;
                }
                line = line.substring(end + 2);
                insideBlockComment = false;
            }

            // 두 기호를 등장 순서대로 본다. // 안의 /* 를 블록 주석 시작으로 오인하면
            // 그 뒤의 import 를 통째로 놓친다.
            while (true) {
                int blockStart = line.indexOf("/*");
                int lineComment = line.indexOf("//");

                if (lineComment >= 0 && (blockStart < 0 || lineComment < blockStart)) {
                    line = line.substring(0, lineComment);
                    break;
                }
                if (blockStart < 0) {
                    break;
                }

                int blockEnd = line.indexOf("*/", blockStart + 2);
                if (blockEnd < 0) {
                    insideBlockComment = true;
                    line = line.substring(0, blockStart);
                    break;
                }
                line = line.substring(0, blockStart) + line.substring(blockEnd + 2);
            }

            if (line.isBlank()) {
                continue;
            }
            if (TYPE_DECLARATION_PATTERN.matcher(line).find()) {
                break;
            }

            Matcher matcher = IMPORT_PATTERN.matcher(line);
            if (matcher.find()) {
                String reference = matcher.group(2);
                declarations.add(new ImportDeclaration(
                    reference,
                    matcher.group(1) != null,
                    reference.endsWith(".*")
                ));
            }
        }
        return declarations;
    }

    record ImportDeclaration(String reference, boolean isStatic, boolean isWildcard) {

        /** 와일드카드의 {@code .*} 와 static import 의 멤버 이름을 떼고 패키지/타입 경로만 남긴다. */
        String withoutTrailingMember() {
            if (isWildcard) {
                return reference.substring(0, reference.length() - 2);
            }
            int lastDot = reference.lastIndexOf('.');
            return lastDot < 0 ? reference : reference.substring(0, lastDot);
        }
    }
}
