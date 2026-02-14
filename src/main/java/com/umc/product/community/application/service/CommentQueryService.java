package com.umc.product.community.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.community.application.port.in.post.CommentInfo;
import com.umc.product.community.application.port.in.post.query.GetCommentListUseCase;
import com.umc.product.community.application.port.out.LoadCommentPort;
import com.umc.product.community.application.port.out.LoadPostPort;
import com.umc.product.community.domain.Comment;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
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

        // 5. 챌린저 ID -> 작성자 이름 매핑
        Map<Long, String> authorNameMap = challengerInfoMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Long memberId = entry.getValue().memberId();
                            var memberProfile = memberProfileMap.get(memberId);
                            return memberProfile != null ? memberProfile.name() : "알 수 없음";
                        }
                ));

        // 6. 챌린저 ID -> 작성자 프로필 이미지 매핑
        Map<Long, String> authorProfileImageMap = challengerInfoMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Long memberId = entry.getValue().memberId();
                            var memberProfile = memberProfileMap.get(memberId);
                            return memberProfile != null ? memberProfile.profileImageLink() : null;
                        }
                ));

        return comments.stream()
                .map(comment -> {
                    String authorName = authorNameMap.get(comment.getChallengerId());
                    String authorProfileImage = authorProfileImageMap.get(comment.getChallengerId());
                    return CommentInfo.from(
                            comment,
                            authorName != null ? authorName : "알 수 없음",
                            authorProfileImage
                    );
                })
                .toList();
    }
}
