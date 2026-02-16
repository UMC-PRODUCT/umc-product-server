package com.umc.product.community.domain;

import com.umc.product.community.domain.enums.Category;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

//해당 부분 공유
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Post {
    @Getter
    private final PostId postId;

    @Getter
    private String title;

    @Getter
    private String content;

    @Getter
    private Category category;

    @Getter
    private final Long authorChallengerId;

    @Getter
    private LightningInfo lightningInfo;

    @Getter
    private final int likeCount;

    @Getter
    private final boolean liked;

    @Getter
    private final Instant createdAt;

    public static Post createPost(String title, String content, Category category, Long authorChallengerId) {
        if (category == Category.LIGHTNING) {
            throw new IllegalArgumentException("번개 게시글은 createLightning()을 사용하세요");
        }
        validateCommonFields(title, content);
        validateAuthorChallengerId(authorChallengerId);
        return new Post(null, title, content, category, authorChallengerId, null, 0, false, null);
    }

    public static Post createLightning(String title, String content, LightningInfo info, Long authorChallengerId) {
        if (info == null) {
            throw new IllegalArgumentException("번개 게시글은 추가 정보가 필수입니다.");
        }
        validateCommonFields(title, content);
        validateAuthorChallengerId(authorChallengerId);
        // 시간 검증은 Service 레이어에서 수행
        return new Post(null, title, content, Category.LIGHTNING, authorChallengerId, info, 0, false, null);
    }

    public static Post reconstruct(PostId postId, String title, String content, Category category,
                                   Long authorChallengerId,
                                   LightningInfo lightningInfo, int likeCount, boolean liked, Instant createdAt) {
        return new Post(postId, title, content, category, authorChallengerId, lightningInfo, likeCount, liked,
            createdAt);
    }

    public boolean isLightning() {
        return category == Category.LIGHTNING;
    }

    public LightningInfo getLightningInfoOrThrow() {
        if (!isLightning() || lightningInfo == null) {
            throw new IllegalStateException("번개 게시글이 아닙니다.");
        }
        return lightningInfo;
    }

    private static void validateCommonFields(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
    }

    private static void validateAuthorChallengerId(Long authorChallengerId) {
        if (authorChallengerId == null) {
            throw new IllegalArgumentException("작성자 ID는 필수입니다");
        }
    }

    public void update(String title, String content, Category category) {
        validateCommonFields(title, content);
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        // 번개 게시글로 카테고리 변경 불가
        if (category == Category.LIGHTNING && this.category != Category.LIGHTNING) {
            throw new IllegalArgumentException("일반 게시글을 번개 게시글로 변경할 수 없습니다.");
        }
        // 번개 게시글에서 일반 게시글로 변경 불가
        if (this.category == Category.LIGHTNING && category != Category.LIGHTNING) {
            throw new IllegalArgumentException("번개 게시글을 일반 게시글로 변경할 수 없습니다.");
        }

        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void updateLightning(String title, String content, LightningInfo newLightningInfo) {
        if (!isLightning()) {
            throw new IllegalStateException("번개 게시글이 아닙니다.");
        }
        validateCommonFields(title, content);
        if (newLightningInfo == null) {
            throw new IllegalArgumentException("번개 정보는 필수입니다.");
        }
        // 시간 검증은 Service 레이어에서 수행

        this.title = title;
        this.content = content;
        this.lightningInfo = newLightningInfo;
    }

    public record PostId(Long id) {
        public PostId {
            if (id <= 0) {
                throw new IllegalArgumentException("ID는 양수여야 합니다.");
            }
        }
    }

    public record LightningInfo(
        LocalDateTime meetAt,
        String location,
        Integer maxParticipants,
        String openChatUrl
    ) {
        public LightningInfo {
            // TODO: 주석 merge conflict로 모두 유지함. 예은이 수정해주세요
          
            // Entity 조회 시에도 생성자가 호출되므로, 비즈니스 로직 검증(미래 시간 체크)은 하지 않음
            // 비즈니스 로직 검증은 Request DTO에서 수행
            // 필수 필드만 검증 (시간 검증은 제거 - 조회 시에도 객체 생성이 필요하므로)
            if (meetAt == null) {
                throw new IllegalArgumentException("모임 시간은 필수입니다.");
            }
            if (location == null || location.isBlank()) {
                throw new IllegalArgumentException("모임 장소는 필수입니다.");
            }
            if (maxParticipants == null || maxParticipants <= 0) {
                throw new IllegalArgumentException("최대 참가자는 1명 이상이어야 합니다.");
            }
            if (openChatUrl == null || openChatUrl.isBlank()) {
                throw new IllegalArgumentException("오픈 채팅 링크는 필수입니다.");
            }
        }

        /**
         * 모임 시간이 현재 이후인지 검증 (Service 레이어에서 호출)
         *
         * @param now 비교할 현재 시간 (테스트 용이성을 위해 외부에서 주입)
         * @throws IllegalArgumentException 모임 시간이 현재 이전인 경우
         */
        public void validateMeetAtIsFuture(LocalDateTime now) {
            if (meetAt.isBefore(now)) {
                throw new IllegalArgumentException("모임 시간은 현재 이후여야 합니다.");
            }
        }
    }
}
