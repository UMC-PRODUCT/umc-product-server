package com.umc.product.community.adapter.in.web.dto.response;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.application.port.in.query.dto.PostInfo;
import com.umc.product.community.domain.Post;
import com.umc.product.community.domain.enums.Category;
import com.umc.product.member.application.port.in.query.MemberInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Builder;

@Schema(description = "게시글 응답")
@Builder
public record PostResponse(
    @Schema(description = "게시글 ID", example = "1")
    Long postId,

    @Schema(description = "제목", example = "스터디원 모집합니다")
    String title,

    @Schema(description = "내용", example = "Spring Boot 스터디원 모집합니다.")
    String content,

    @Schema(description = "카테고리", example = "FREE")
    Category category,

    @Schema(description = "작성자 ID", example = "123", deprecated = true)
    Long authorId,

    @Schema(description = "작성자 챌린저 ID", example = "123")
    Long authorChallengerId,

    @Schema(description = "작성자 회원 ID", example = "123")
    Long authorMemberId,

    @Schema(description = "작성자 이름", example = "홍길동")
    String authorName,

    @Schema(description = "작성자 프로필 이미지", example = "https://example.com/profile.jpg")
    String authorProfileImage,

    @Schema(description = "작성자 파트", example = "SPRINGBOOT")
    ChallengerPart authorPart,

    @Schema(description = "작성일시", example = "2026-02-13T10:30:00Z")
    Instant createdAt,

    @Schema(description = "댓글 수", example = "5")
    int commentCount,

    @Schema(description = "좋아요 수", example = "42")
    int likeCount,

    @Schema(description = "좋아요 여부", example = "true")
    boolean isLiked,

    @Schema(description = "본인 작성 글 여부", example = "true")
    boolean isAuthor,

    @Schema(description = "번개 정보 (번개글인 경우)")
    LightningInfoResponse lightningInfo
) {
    public static PostResponse from(Post post, MemberInfo memberInfo, ChallengerInfo challengerInfo) {
        return PostResponse.builder()
            .postId(post.getPostId().id())
            .title(post.getTitle())
            .content(post.getContent())
            .category(post.getCategory())
            .authorId(challengerInfo.challengerId()) // 검색 결과에는 작성자 정보가 없으므로 null로 설정
            .authorChallengerId(challengerInfo.challengerId())
            .authorMemberId(memberInfo.id())
            .authorName(memberInfo.name())
            .authorProfileImage(memberInfo.profileImageLink())
            .authorPart(challengerInfo.part())
            .createdAt(post.getCreatedAt())
            .commentCount(0) // TODO: 리팩토링 후에 다시 하자 ...
            .likeCount(post.getLikeCount())
            .isLiked(false) // 검색 결과에서는 좋아요 여부를 알 수 없으므로 false로 설정
            .isAuthor(false) // 검색 결과에서는 본인 작성 여부를 알 수 없으므로 false로 설정
            .lightningInfo(null) // 검색 결과에서는 번개 정보가 없으므로 null로 설정
            .build();
    }

    // ======================================================
    // ====================== 예은 작업본 ======================
    // ======================================================

    public static PostResponse from(PostInfo info) {
        LightningInfoResponse lightningInfoResponse = null;

        if (info.category() == Category.LIGHTNING) {
            lightningInfoResponse = LightningInfoResponse.builder()
                .meetAt(info.meetAt())
                .location(info.location())
                .maxParticipants(info.maxParticipants())
                .openChatUrl(info.openChatUrl())
                .build();
        }

        return PostResponse.builder()
            .postId(info.postId())
            .title(info.title())
            .content(info.content())
            .category(info.category())
            .authorId(info.authorChallengerId()) // authorId 필드 추가
            .authorChallengerId(info.authorChallengerId())
            .authorMemberId(null) // TODO: 이거 null값 안 들어가도록 수정하기
            .authorName(info.authorName())
            .authorProfileImage(info.authorProfileImage())
            .authorPart(info.authorPart())
            .createdAt(info.createdAt())
            .commentCount(info.commentCount())
            .likeCount(info.likeCount())
            .isLiked(info.isLiked())
            .isAuthor(info.isAuthor())
            .lightningInfo(lightningInfoResponse)
            .build();
    }

    @Schema(description = "번개 정보")
    @Builder
    public record LightningInfoResponse(
        @Schema(description = "모임 시간", example = "2026-03-16T18:00:00")
        LocalDateTime meetAt,

        @Schema(description = "모임 장소", example = "강남역 2번 출구")
        String location,

        @Schema(description = "최대 참가자 수", example = "5")
        Integer maxParticipants,

        @Schema(description = "오픈 채팅 링크", example = "https://open.kakao.com/o/sxxxxxx")
        String openChatUrl
    ) {
    }
}
