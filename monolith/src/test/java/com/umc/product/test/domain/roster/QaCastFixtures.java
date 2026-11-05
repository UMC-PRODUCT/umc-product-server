package com.umc.product.test.domain.roster;

import java.util.Map;

import com.umc.product.test.adapter.out.resource.ClasspathQaRosterAdapter;

public final class QaCastFixtures {

    /** V2026.09.10.03.00 마이그레이션의 11기 지부 배치. */
    public static final Map<String, String> CHAPTER_BY_SCHOOL = Map.ofEntries(
        Map.entry("가천대학교", "캥거루"), Map.entry("서울여자대학교", "캥거루"),
        Map.entry("한국항공대학교", "캥거루"), Map.entry("한성대학교", "캥거루"),
        Map.entry("동양미래대학교", "쿼카"), Map.entry("숭실대학교", "쿼카"), Map.entry("이화여자대학교", "쿼카"),
        Map.entry("인하대학교", "쿼카"), Map.entry("한양대학교 ERICA", "쿼카"),
        Map.entry("성신여자대학교", "수달"), Map.entry("숙명여자대학교", "수달"), Map.entry("중앙대학교", "수달"),
        Map.entry("한국외국어대학교", "수달"),
        Map.entry("가톨릭대학교", "아홀로틀"), Map.entry("덕성여자대학교", "아홀로틀"), Map.entry("세종대학교", "아홀로틀"),
        Map.entry("안양대학교", "아홀로틀"), Map.entry("홍익대학교 서울캠퍼스", "아홀로틀"),
        Map.entry("단국대학교", "티라노"), Map.entry("동국대학교", "티라노"), Map.entry("동덕여자대학교", "티라노"),
        Map.entry("서경대학교", "티라노"), Map.entry("홍익대학교 세종캠퍼스", "티라노"));

    private QaCastFixtures() {
    }

    public static RehearsalCast cast() {
        return RehearsalCast.of(new ClasspathQaRosterAdapter().load(), CHAPTER_BY_SCHOOL);
    }
}
