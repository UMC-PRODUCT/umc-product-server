package com.umc.product.test.application.service.qa;

import java.util.Map;

public record QaSeedContext(
    Map<String, Long> members,
    Map<String, Long> challengers,
    Map<Integer, Long> gisus,
    Map<String, Long> schools,
    Map<String, Long> chapters
) {
    public QaSeedContext {
        members = Map.copyOf(members);
        challengers = Map.copyOf(challengers);
        gisus = Map.copyOf(gisus);
        schools = Map.copyOf(schools);
        chapters = Map.copyOf(chapters);
    }

    public Long memberId(String alias) {
        return required(members, alias);
    }

    public Long challengerId(String alias, int generation) {
        return required(challengers, alias + ":" + generation);
    }

    public Long gisuId(int generation) {
        return required(gisus, generation);
    }

    public Long schoolId(String name) {
        return required(schools, name);
    }

    public Long chapterId(int generation, String name) {
        return required(chapters, generation + ":" + name);
    }

    private static <K> Long required(Map<K, Long> values, K key) {
        Long value = values.get(key);
        if (value == null) {
            throw new IllegalArgumentException("QA 시딩 참조가 없습니다: " + key);
        }
        return value;
    }
}
