package com.umc.product.community.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.community.application.port.in.post.CommentInfo;
import com.umc.product.community.application.port.in.post.query.GetCommentListUseCase;
import com.umc.product.community.application.port.out.LoadCommentPort;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.domain.Comment;
import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService implements GetCommentListUseCase {

    private final LoadPostPort loadPostPort;
    private final LoadCommentPort loadCommentPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public List<CommentInfo> getComments(Long postId) {
        return getComments(postId, null);
    }

    @Override
    public List<CommentInfo> getComments(Long postId, Long currentChallengerId) {
        loadPostPort.findById(postId)
            .orElseThrow(() -> new BusinessException(Domain.COMMUNITY, CommunityErrorCode.POST_NOT_FOUND));

        Page<Comment> comments = loadCommentPort.findByPostId(postId, Pageable.unpaged());

        if (comments.isEmpty()) {
            return List.of();
        }

        // 1. 챌린저 ID 목록 추출
        Set<Long> challengerIds = comments.stream()
            .map(Comment::getChallengerId)
            .collect(Collectors.toSet());

        // 2. 챌린저 ID -> 챌린저 정보 매핑
        Map<Long, ChallengerInfo> challengerInfoMap = getChallengerUseCase.getChallengerPublicInfoByIds(challengerIds);

        // 3. 멤버 ID 목록 추출
        Set<Long> memberIds = challengerInfoMap.values().stream()
            .map(ChallengerInfo::memberId)
            .collect(Collectors.toSet());

        // 4. 멤버 ID -> 멤버 프로필 매핑 (1 query, 일괄 조회로 N+1 해결)
        var memberProfileMap = getMemberUseCase.getProfiles(memberIds);

        // 5. 챌린저 ID -> 작성자 정보 매핑 (이름 + 프로필 이미지 + 파트)
        Map<Long, AuthorDetails> authorDetailsMap = challengerInfoMap.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    Long memberId = entry.getValue().memberId();
                    var memberProfile = memberProfileMap.get(memberId);
                    String name = memberProfile != null ? memberProfile.name() : "알 수 없음";
                    String profileImage = memberProfile != null ? memberProfile.profileImageLink() : null;
                    return new AuthorDetails(name, profileImage, entry.getValue().part());
                }
            ));

        return comments.stream()
            .map(comment -> {
                AuthorDetails authorDetails = authorDetailsMap.get(comment.getChallengerId());
                String authorName = authorDetails != null ? authorDetails.name() : "알 수 없음";
                String authorProfileImage = authorDetails != null ? authorDetails.profileImage() : null;
                var authorPart = authorDetails != null ? authorDetails.part() : null;
                // 본인 작성 댓글 여부 확인
                boolean isAuthor = currentChallengerId != null && comment.getChallengerId().equals(currentChallengerId);
                return CommentInfo.from(
                    comment,
                    authorName,
                    authorProfileImage,
                    authorPart,
                    isAuthor
                );
            })
            .toList();
    }

    @Override
    public CommentInfo getComment(Long commentId) {
        // TODO: 이거 CommentInfo에 isAuthor 필드가 있으면 안 될 것 같다는 의견이긴 합니다. 예은이는 어떻게 생각하시나요? by 경운.

        return loadCommentPort.findById(commentId)
            .map(comment -> {
                ChallengerInfo challengerInfo = getChallengerUseCase.getChallengerPublicInfo(comment.getChallengerId());
                MemberInfo memberInfo = getMemberUseCase.getMemberInfoById(challengerInfo.memberId());

                String authorName = memberInfo != null ? memberInfo.name() : "알 수 없음";
                String authorProfileImage = memberInfo != null ? memberInfo.profileImageLink() : null;

                return CommentInfo.from(
                    comment,
                    authorName,
                    authorProfileImage,
                    challengerInfo.part(),
                    false // 단일 댓글 조회에서는 작성자 여부 판단이 어려움 (currentChallengerId가 없기 때문)
                );
            })
            .orElseThrow(() -> new CommunityDomainException(CommunityErrorCode.COMMENT_NOT_FOUND));
    }

    /**
     * 작성자 정보를 담는 내부 record
     */
    private record AuthorDetails(String name, String profileImage, ChallengerPart part) {
    }
}
