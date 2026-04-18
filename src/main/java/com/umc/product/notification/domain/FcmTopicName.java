package com.umc.product.notification.domain;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * FCM 토픽 네이밍 규칙을 관리하는 클래스
 *
 * @deprecated 토큰 기반 알림(SendNotificationToAudienceUseCase)으로 전환됨. 사용 X
 */
@Deprecated(since = "token-based migration", forRemoval = true)
@Component
public class FcmTopicName {

    private final String prefix;

    public FcmTopicName(@Value("${app.fcm.topic-prefix:local}") String prefix) {
        this.prefix = prefix;
    }

    private String withPrefix(String topic) {
        return prefix + "-" + topic;
    }

    public String all() {
        return withPrefix("all");
    }

    public String school(Long schoolId) {
        return withPrefix("school-" + schoolId);
    }

    public String gisu(Long gisuId) {
        return withPrefix("gisu-" + gisuId);
    }

    public String gisuPart(Long gisuId, ChallengerPart part) {
        return withPrefix("gisu-" + gisuId + "-part-" + part.name());
    }

    public String gisuSchool(Long gisuId, Long schoolId) {
        return withPrefix("gisu-" + gisuId + "-school-" + schoolId);
    }

    public String gisuSchoolPart(Long gisuId, Long schoolId, ChallengerPart part) {
        return withPrefix("gisu-" + gisuId + "-school-" + schoolId + "-part-" + part.name());
    }

    public String gisuChapter(Long gisuId, Long chapterId) {
        return withPrefix("gisu-" + gisuId + "-chapter-" + chapterId);
    }

    public String gisuChapterPart(Long gisuId, Long chapterId, ChallengerPart part) {
        return withPrefix("gisu-" + gisuId + "-chapter-" + chapterId + "-part-" + part.name());
    }

    /**
     * 개인 알림용 토픽 (prefix 포함, 예: "dev-member-2")
     */
    public String member(Long memberId) {
        return withPrefix("member-" + memberId);
    }

    /**
     * 챌린저가 구독해야 할 모든 토픽 목록을 반환 (prefix 포함)
     */
    public List<String> allTopicsFor(Long gisuId, ChallengerPart part, Long schoolId, Long chapterId) {
        List<String> topics = new ArrayList<>();
        topics.add(all());
        topics.add(gisu(gisuId));
        topics.add(gisuPart(gisuId, part));

        if (schoolId != null) {
            topics.add(school(schoolId));
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
     * 챌린저가 구독해야 할 모든 토픽 목록을 반환 (prefix 없는 버전)
     * <p>
     * prefix 없이 구독했던 레거시 토픽들을 일괄 해제할 때 사용
     */
    public List<String> allTopicsForWithoutPrefix(Long gisuId, ChallengerPart part, Long schoolId, Long chapterId) {
        List<String> topics = new ArrayList<>();
        topics.add("all");
        topics.add("gisu-" + gisuId);
        topics.add("gisu-" + gisuId + "-part-" + part.name());

        if (schoolId != null) {
            topics.add("school-" + schoolId);
            topics.add("gisu-" + gisuId + "-school-" + schoolId);
            topics.add("gisu-" + gisuId + "-school-" + schoolId + "-part-" + part.name());
        }

        if (chapterId != null) {
            topics.add("gisu-" + gisuId + "-chapter-" + chapterId);
            topics.add("gisu-" + gisuId + "-chapter-" + chapterId + "-part-" + part.name());
        }

        return topics;
    }

    /**
     * NoticeTargetInfo 기반으로 발행할 토픽 이름 목록을 반환 파트가 여러 개이면 각 파트별 토픽을 생성
     */
    public List<String> resolveTopics(
        Long gisuId, Long chapterId, Long schoolId, List<ChallengerPart> parts
    ) {
        boolean hasParts = parts != null && !parts.isEmpty();

        if (gisuId == null) {
            if (schoolId != null) {
                return List.of(school(schoolId));
            }
            return List.of(all());
        }

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
