package com.umc.product.community.domain;

import com.umc.product.community.domain.enums.Category;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
    private final LightningInfo lightningInfo;

    @Getter
    private final int likeCount;

    @Getter
    private final boolean liked;

    public static Post createPost(String title, String content, Category category, Long authorChallengerId) {
        if (category == Category.LIGHTNING) {
            throw new IllegalArgumentException("번개 게시글은 createLightning()을 사용하세요");
        }
        if (authorChallengerId == null) {
            throw new IllegalArgumentException("작성자 ID는 필수입니다");
        }
        validateCommonFields(title, content);
        return new Post(null, title, content, category, authorChallengerId, null, 0, false);
    }

    public static Post createLightning(String title, String content, LightningInfo info, Long authorChallengerId) {
        if (info == null) {
            throw new IllegalArgumentException("번개 게시글은 추가 정보가 필수입니다.");
        }
        if (authorChallengerId == null) {
            throw new IllegalArgumentException("작성자 ID는 필수입니다");
        }
        validateCommonFields(title, content);
        return new Post(null, title, content, Category.LIGHTNING, authorChallengerId, info, 0, false);
    }

    public static Post reconstruct(PostId postId, String title, String content, Category category, Long authorChallengerId,
                                   LightningInfo lightningInfo, int likeCount, boolean liked) {
        return new Post(postId, title, content, category, authorChallengerId, lightningInfo, likeCount, liked);
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

    public void update(String title, String content, Category category) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }
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
            if (meetAt == null || meetAt.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("모임 시간은 현재 이후여야 합니다.");
            }
            if (location == null || location.isBlank()) {
                throw new IllegalArgumentException("모임 장소는 필수입니다.");
            }
            if (maxParticipants <= 0) {
                throw new IllegalArgumentException("최대 참가자는 1명 이상이어야 합니다.");
            }
            if (openChatUrl == null || openChatUrl.isBlank()) {
                throw new IllegalArgumentException("오픈 채팅 링크는 필수입니다.");
            }
        }
    }
}
