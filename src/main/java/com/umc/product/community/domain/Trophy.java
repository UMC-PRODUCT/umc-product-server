package com.umc.product.community.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Trophy {
    //챌린저 학교 파트 가져와야함 기둘
    @Getter
    private TrophyId thophyid;

    @Getter
    private final ChallengerId challengerId;
    //우선 요래둠 받아오는거 보고 움직이기

    @Getter
    private int week;

    @Getter
    private String title;

    @Getter
    private final String url;

    @Getter
    private final String content;

    public static Trophy create(int week, ChallengerId challengerId, String title, String content, String url) {
        validate(week, challengerId, title, url);
        return new Trophy(null, challengerId, week, title, url, content);
    }

    public static Trophy reconstruct(TrophyId trophyId, ChallengerId challengerId, int week, String title,
                                     String content, String url) {
        return new Trophy(trophyId, challengerId, week, title, url, content);
    }

    private static void validate(int week, ChallengerId challengerId, String title, String url) {
        if (challengerId == null) {
            throw new IllegalArgumentException("챌린저 정보는 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("링크는 필수입니다.");
        }
    }

    public record TrophyId(long id) {
        public TrophyId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }

    public record ChallengerId(long id) {
        public ChallengerId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }
}
