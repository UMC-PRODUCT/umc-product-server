package com.umc.product.community.domain;

import com.umc.product.community.domain.enums.Category;
import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import java.time.Instant;
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
    private final boolean liked; // TODO:

    @Getter
    private final Instant createdAt;

    // ============== 예은 ==============

    public static Post createPost(String title, String content, Category category, Long authorChallengerId) {
        if (category.isLightning()) {
            throw new CommunityDomainException(CommunityErrorCode.USE_LIGHTNING_API);
        }
        validateCommonFields(title, content);
        validateAuthorChallengerId(authorChallengerId);
        return new Post(null, title, content, category, authorChallengerId, null, 0, false, null);
    }

    public static Post createLightning(String title, String content, LightningInfo info, Long authorChallengerId) {
        if (info == null) {
            throw new CommunityDomainException(CommunityErrorCode.LIGHTNING_INFO_REQUIRED);
        }

        validateCommonFields(title, content);
        validateAuthorChallengerId(authorChallengerId);
        // 시간 검증은 Service 레이어에서 수행
        return new Post(null, title, content, Category.LIGHTNING, authorChallengerId, info, 0, false, null);
    }

    // TODO: 이건 정체가 뭐임? - 경운
    public static Post reconstruct(
        PostId postId, String title, String content, Category category,
        Long authorChallengerId, LightningInfo lightningInfo,
        int likeCount, boolean liked, Instant createdAt) {
        return new Post(postId, title, content, category, authorChallengerId, lightningInfo, likeCount, liked,
            createdAt);
    }

    public boolean isLightning() {
        return this.category == Category.LIGHTNING;
    }

    public LightningInfo getLightningInfoOrThrow() {
        if (!isLightning() || lightningInfo == null) {
            throw new CommunityDomainException(CommunityErrorCode.NOT_LIGHTNING_POST);
        }
        return lightningInfo;
    }

    private static void validateCommonFields(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new CommunityDomainException(CommunityErrorCode.INVALID_POST_TITLE);
        }
        if (content == null || content.isBlank()) {
            throw new CommunityDomainException(CommunityErrorCode.INVALID_POST_CONTENT);
        }
    }

    private static void validateAuthorChallengerId(Long authorChallengerId) {
        if (authorChallengerId == null) {
            throw new CommunityDomainException(CommunityErrorCode.INVALID_POST_AUTHOR);
        }
    }

    public void update(String title, String content, Category category) {
        validateCommonFields(title, content);
        if (category == null) {
            throw new CommunityDomainException(CommunityErrorCode.INVALID_POST_CATEGORY);
        }
        // 번개 게시글로 카테고리 변경 불가
        if (category == Category.LIGHTNING && this.category != Category.LIGHTNING) {
            throw new CommunityDomainException(CommunityErrorCode.CANNOT_CHANGE_TO_LIGHTNING);
        }
        // 번개 게시글에서 일반 게시글로 변경 불가
        if (this.category == Category.LIGHTNING && category != Category.LIGHTNING) {
            throw new CommunityDomainException(CommunityErrorCode.CANNOT_CHANGE_FROM_LIGHTNING);
        }

        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void updateLightning(String title, String content, LightningInfo newLightningInfo) {
        if (!isLightning()) {
            throw new CommunityDomainException(CommunityErrorCode.NOT_LIGHTNING_POST);
        }
        validateCommonFields(title, content);
        if (newLightningInfo == null) {
            throw new CommunityDomainException(CommunityErrorCode.LIGHTNING_INFO_REQUIRED);
        }
        // 시간 검증은 Service 레이어에서 수행

        this.title = title;
        this.content = content;
        this.lightningInfo = newLightningInfo;
    }

    @Builder
    public record PostId(Long id) {
        public PostId {
            if (id <= 0) {
                throw new CommunityDomainException(CommunityErrorCode.INVALID_ID);
            }
        }
    }

    @Builder
    public record LightningInfo(
        Instant meetAt,
        String location,
        Integer maxParticipants,
        String openChatUrl
    ) {
        public LightningInfo {
            // Entity 조회 시에도 생성자가 호출되므로, 비즈니스 로직 검증(미래 시간 체크)은 하지 않음
            // 비즈니스 로직 검증은 Request DTO에서 수행
            // 필수 필드만 검증
            if (meetAt == null) {
                throw new CommunityDomainException(CommunityErrorCode.INVALID_LIGHTNING_MEET_AT);
            }
            if (location == null || location.isBlank()) {
                throw new CommunityDomainException(CommunityErrorCode.INVALID_LIGHTNING_LOCATION);
            }
            if (maxParticipants == null || maxParticipants <= 0) {
                throw new CommunityDomainException(CommunityErrorCode.INVALID_LIGHTNING_MAX_PARTICIPANTS);
            }
            if (openChatUrl == null || openChatUrl.isBlank()) {
                throw new CommunityDomainException(CommunityErrorCode.INVALID_LIGHTNING_OPEN_CHAT_URL);
            }
            if (!openChatUrl.startsWith("https://") && !openChatUrl.startsWith("http://")) {
                throw new CommunityDomainException(CommunityErrorCode.INVALID_LIGHTNING_OPEN_CHAT_URL_FORMAT);
            }
        }

        /**
         * 모임 시간이 현재 이후인지 검증 (Service 레이어에서 호출)
         *
         * @param now 비교할 현재 시간 (테스트 용이성을 위해 외부에서 주입)
         * @throws CommunityDomainException 모임 시간이 현재 이전인 경우
         */
        public void validateMeetAtIsFuture(Instant now) {
            if (meetAt.isBefore(now)) {
                throw new CommunityDomainException(CommunityErrorCode.INVALID_LIGHTNING_MEET_AT_PAST);
            }
        }
    }
}
