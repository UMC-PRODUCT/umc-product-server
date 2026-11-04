package com.umc.product.test.application.service.qa;

import java.util.Map;

import com.umc.product.test.domain.roster.QaAccountPlan;
import com.umc.product.test.domain.roster.QaPersonas.Persona;
import com.umc.product.test.domain.roster.QaRoster;

/**
 * 단계가 시작될 때 한 번 읽은 QA 계정 ID와 기수·학교·지부 ID. 단계마다 새로 읽어 앞 단계가 만든 계정을 반영한다.
 */
record QaDirectory(
    QaRoster roster,
    QaAccountPlan accounts,
    String emailDomain,
    Map<String, Long> memberIdsByEmail,
    Map<Integer, Long> gisuIds,
    Map<String, Long> schoolIds,
    Map<String, Long> chapterIds,
    Map<String, Long> chapterIdsBySchool
) {
    QaDirectory {
        memberIdsByEmail = Map.copyOf(memberIdsByEmail);
        gisuIds = Map.copyOf(gisuIds);
        schoolIds = Map.copyOf(schoolIds);
        chapterIds = Map.copyOf(chapterIds);
        chapterIdsBySchool = Map.copyOf(chapterIdsBySchool);
    }

    Long memberId(Persona persona) {
        String email = persona.email(roster, emailDomain);
        Long memberId = memberIdsByEmail.get(email);
        if (memberId == null) {
            throw new IllegalStateException("QA 계정이 없습니다. 계정 단계를 먼저 실행해주세요: " + email);
        }
        return memberId;
    }

    Long findMemberId(String email) {
        return memberIdsByEmail.get(email);
    }

    Long gisuId(int generation) {
        return required(gisuIds, generation, generation + "기");
    }

    Long schoolId(String schoolName) {
        return required(schoolIds, schoolName, schoolName);
    }

    Long chapterId(int generation, String chapterName) {
        return required(chapterIds, generation + ":" + chapterName, generation + "기 " + chapterName + " 지부");
    }

    Long chapterIdOfSchool(int generation, Long schoolId) {
        return required(chapterIdsBySchool, generation + ":" + schoolId, generation + "기 학교 " + schoolId + "의 지부");
    }

    private static <K> Long required(Map<K, Long> values, K key, String label) {
        Long value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("QA 시딩 기준 데이터가 없습니다: " + label);
        }
        return value;
    }
}
