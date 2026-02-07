package com.umc.product.community.adapter.out.persistence;

import com.umc.product.common.BaseEntity;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.Post.PostId;
import com.umc.product.community.domain.enums.Category;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private boolean anonymous;

    // Lightning 관련 필드 (nullable)
    private LocalDateTime meetAt;

    private String location;

    private Integer maxParticipants;

    @ElementCollection
    @CollectionTable(name = "post_like", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "challenger_id")
    private Set<Long> likedChallengerIds = new HashSet<>();

    private PostJpaEntity(String title, String content, Category category, String region,
                          boolean anonymous, LocalDateTime meetAt, String location, Integer maxParticipants) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.region = region;
        this.anonymous = anonymous;
        this.meetAt = meetAt;
        this.location = location;
        this.maxParticipants = maxParticipants;
    }

    private static final String DEFAULT_REGION = "";
    private static final boolean DEFAULT_ANONYMOUS = false;

    public static PostJpaEntity from(Post post) {
        LocalDateTime meetAt = null;
        String location = null;
        Integer maxParticipants = null;

        if (post.isLightning() && post.getLightningInfo() != null) {
            Post.LightningInfo info = post.getLightningInfo();
            meetAt = info.meetAt();
            location = info.location();
            maxParticipants = info.maxParticipants();
        }

        return new PostJpaEntity(
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                DEFAULT_REGION,
                DEFAULT_ANONYMOUS,
                meetAt,
                location,
                maxParticipants
        );
    }

    public Post toDomain() {
        return toDomain(null);
    }

    public Post toDomain(Long viewerChallengerId) {
        Post.LightningInfo lightningInfo = null;
        if (category == Category.LIGHTNING && meetAt != null) {
            lightningInfo = new Post.LightningInfo(meetAt, location, maxParticipants);
        }

        boolean liked = viewerChallengerId != null && isLikedBy(viewerChallengerId);

        return Post.reconstruct(
                new PostId(id),
                title,
                content,
                category,
                region,
                anonymous,
                lightningInfo,
                getLikeCount(),
                liked
        );
    }

    public void update(String title, String content, Category category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public boolean toggleLike(Long challengerId) {
        if (!likedChallengerIds.remove(challengerId)) {
            likedChallengerIds.add(challengerId);
            return true;
        }
        return false;
    }

    public int getLikeCount() {
        return likedChallengerIds.size();
    }

    public boolean isLikedBy(Long challengerId) {
        return likedChallengerIds.contains(challengerId);
    }
}
