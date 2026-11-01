package com.umc.product.community.application.policy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * PM이 작성해 전달하는 금칙어 목록(csv)을 서버 시작 시 메모리에 올려두고, 메시지 본문에 포함됐는지 검사한다.
 *
 * <p>목록이 수백 건 수준이라 DB·캐시 없이 리소스 파일 하나로 충분하다. 목록이 바뀌면 이 csv 파일을
 * 교체해 배포한다.</p>
 */
@Component
public class ForbiddenWordFilter {

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
        return forbiddenWords.stream().anyMatch(content::contains);
    }

    private static Set<String> load(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                .skip(1) // header: id,term,category
                .map(line -> line.split(",", 3))
                .filter(columns -> columns.length >= 2 && !columns[1].isBlank())
                .map(columns -> columns[1].trim())
                .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new IllegalStateException("금칙어 목록을 불러오지 못했습니다.", e);
        }
    }
}
