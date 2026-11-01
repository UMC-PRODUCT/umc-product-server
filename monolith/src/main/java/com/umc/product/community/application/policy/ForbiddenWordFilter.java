package com.umc.product.community.application.policy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * PM이 작성해 전달하는 금칙어 목록(csv)을 서버 시작 시 메모리에 올려두고, 메시지 본문에 포함됐는지 검사한다.
 *
 * <p>목록이 수백 건 수준이라 DB·캐시 없이 리소스 파일 하나로 충분하다. 목록이 바뀌면 이 csv 파일을
 * 교체해 배포한다.</p>
 *
 * <p>공백·기호·대소문자로 끼워 넣는 우회("씨 발", "병.신", "NIGGER")를 막기 위해 목록과 본문을 같은
 * 방식으로 정규화한 뒤 비교한다.</p>
 */
@Component
public class ForbiddenWordFilter {

    // 공백·ASCII 기호·가운뎃점류·대시류·폭 0 문자(ZWSP/ZWNJ/ZWJ/BOM)를 제거 대상으로 본다.
    private static final Pattern NOISE_PATTERN = Pattern.compile(
        "[\\s\\p{Punct}\u00B7\u318D\u2013\u2014\u200B\u200C\u200D\uFEFF]+"
    );

    private final Set<String> forbiddenWords;

    public ForbiddenWordFilter(
        @Value("classpath:community/forbidden-words.csv") Resource forbiddenWordsResource
    ) {
        this.forbiddenWords = load(forbiddenWordsResource);
    }

    public boolean containsForbiddenWord(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }
        String normalized = normalize(content);
        return forbiddenWords.stream().anyMatch(normalized::contains);
    }

    private static Set<String> load(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                .skip(1) // header: id,term,category
                .map(line -> line.split(",", 3))
                .filter(columns -> columns.length >= 2 && !columns[1].isBlank())
                .map(columns -> normalize(columns[1].trim()))
                .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new IllegalStateException("금칙어 목록을 불러오지 못했습니다.", e);
        }
    }

    private static String normalize(String text) {
        return NOISE_PATTERN.matcher(text).replaceAll("").toLowerCase();
    }
}
