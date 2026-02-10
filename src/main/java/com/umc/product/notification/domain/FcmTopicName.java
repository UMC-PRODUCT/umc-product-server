package com.umc.product.notification.domain;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.ArrayList;
import java.util.List;

/**
 * FCM 토픽 네이밍 규칙을 관리하는 유틸리티 클래스
 *
 * 토픽명 예시:
 * - gisu-1 (기수 전체)
 * - gisu-1-part-SPRINGBOOT (기수 + 파트)
 * - gisu-1-school-5 (기수 + 학교)
 * - gisu-1-school-5-part-WEB (기수 + 학교 + 파트)
 * - gisu-1-chapter-3 (기수 + 지부)
 * - gisu-1-chapter-3-part-ANDROID (기수 + 지부 + 파트)
 */
public final class FcmTopicName {

    private FcmTopicName() {
    }

    public static String gisu(Long gisuId) {
        return "gisu-" + gisuId;
    }

    public static String gisuPart(Long gisuId, ChallengerPart part) {
        return "gisu-" + gisuId + "-part-" + part.name();
    }

    public static String gisuSchool(Long gisuId, Long schoolId) {
        return "gisu-" + gisuId + "-school-" + schoolId;
    }

    public static String gisuSchoolPart(Long gisuId, Long schoolId, ChallengerPart part) {
        return "gisu-" + gisuId + "-school-" + schoolId + "-part-" + part.name();
    }

    public static String gisuChapter(Long gisuId, Long chapterId) {
        return "gisu-" + gisuId + "-chapter-" + chapterId;
    }

    public static String gisuChapterPart(Long gisuId, Long chapterId, ChallengerPart part) {
        return "gisu-" + gisuId + "-chapter-" + chapterId + "-part-" + part.name();
    }

    /**
     * 챌린저가 구독해야 할 모든 토픽 목록을 반환
     */
    public static List<String> allTopicsFor(Long gisuId, ChallengerPart part, Long schoolId, Long chapterId) {
        List<String> topics = new ArrayList<>();
        topics.add(gisu(gisuId));
        topics.add(gisuPart(gisuId, part));

        if (schoolId != null) {
            topics.add(gisuSchool(gisuId, schoolId));
            topics.add(gisuSchoolPart(gisuId, schoolId, part));
        }

        if (chapterId != null) {
            topics.add(gisuChapter(gisuId, chapterId));
            topics.add(gisuChapterPart(gisuId, chapterId, part));
        }

        return topics;
    }

    /**
     * NoticeTargetInfo 기반으로 발행할 토픽 이름 목록을 반환
     * 파트가 여러 개이면 각 파트별 토픽을 생성
     */
    public static List<String> resolveTopics(
            Long gisuId, Long chapterId, Long schoolId, List<ChallengerPart> parts
    ) {
        boolean hasParts = parts != null && !parts.isEmpty();

        if (chapterId != null) {
            if (hasParts) {
                return parts.stream()
                        .map(part -> gisuChapterPart(gisuId, chapterId, part))
                        .toList();
            }
            return List.of(gisuChapter(gisuId, chapterId));
        }

        if (schoolId != null) {
            if (hasParts) {
                return parts.stream()
                        .map(part -> gisuSchoolPart(gisuId, schoolId, part))
                        .toList();
            }
            return List.of(gisuSchool(gisuId, schoolId));
        }

        if (hasParts) {
            return parts.stream()
                    .map(part -> gisuPart(gisuId, part))
                    .toList();
        }

        return List.of(gisu(gisuId));
    }
}
