package com.umc.product.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.community.application.port.in.command.comment.CreateCommentUseCase;
import com.umc.product.community.application.port.in.command.comment.dto.CreateCommentCommand;
import com.umc.product.community.application.port.in.command.post.CreatePostUseCase;
import com.umc.product.community.application.port.in.command.post.dto.CreatePostCommand;
import com.umc.product.community.application.port.in.command.trophy.CreateTrophyUseCase;
import com.umc.product.community.application.port.in.command.trophy.dto.CreateTrophyCommand;
import com.umc.product.community.application.port.in.query.dto.CommentInfo;
import com.umc.product.community.application.port.in.query.dto.PostInfo;
import com.umc.product.community.application.port.in.query.dto.TrophyInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.test.application.port.in.command.dto.SeedCommunityCommand;
import com.umc.product.test.application.port.in.command.dto.SeedCommunityResult;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommunitySeedServiceTest {

    @Mock
    DummyCommunityFactory dummyCommunityFactory;
    @Mock
    GetGisuUseCase getGisuUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;
    @Mock
    CreatePostUseCase createPostUseCase;
    @Mock
    CreateCommentUseCase createCommentUseCase;
    @Mock
    CreateTrophyUseCase createTrophyUseCase;

    @InjectMocks
    CommunitySeedService sut;

    @BeforeEach
    void setUp() {
        lenient().when(dummyCommunityFactory.nextPostCommand(anyLong()))
            .thenReturn(mock(CreatePostCommand.class));
        lenient().when(dummyCommunityFactory.nextCommentCommand(anyLong(), anyLong()))
            .thenReturn(mock(CreateCommentCommand.class));
        lenient().when(dummyCommunityFactory.nextTrophyCommand(anyLong()))
            .thenReturn(mock(CreateTrophyCommand.class));
    }

    @Test
    @DisplayName("챌린저 풀이 비어있으면 skipped=true 로 반환한다")
    void 챌린저_풀_비어있으면_스킵() {
        // Given
        given(getChallengerUseCase.getAllByGisuId(9L)).willReturn(List.of());

        // When
        SeedCommunityResult result = sut.seed(new SeedCommunityCommand(9L, 5, 3, 2));

        // Then
        assertThat(result.skipped()).isTrue();
        assertThat(result.reason()).contains("no challengers");
        verify(createPostUseCase, never()).createPost(any());
        verify(createCommentUseCase, never()).create(any());
        verify(createTrophyUseCase, never()).createTrophy(any());
    }

    @Test
    @DisplayName("정상 시딩 시 post / comment / trophy 가 요청 수량만큼 생성된다")
    void 정상_시딩_수량_확인() {
        // Given
        Long gisuId = 9L;
        given(getChallengerUseCase.getAllByGisuId(gisuId)).willReturn(challengers(5));
        givenIdCountersForCreates();

        // When
        SeedCommunityResult result = sut.seed(new SeedCommunityCommand(gisuId, 4, 2, 3));

        // Then
        assertThat(result.skipped()).isFalse();
        assertThat(result.gisuId()).isEqualTo(gisuId);
        assertThat(result.createdPostIds()).hasSize(4);
        assertThat(result.createdCommentIds()).hasSize(4 * 2);
        assertThat(result.createdTrophyIds()).hasSize(3);
        verify(createPostUseCase, times(4)).createPost(any());
        verify(createCommentUseCase, times(8)).create(any());
        verify(createTrophyUseCase, times(3)).createTrophy(any());
    }

    @Test
    @DisplayName("post 생성 실패는 다음 단계 시딩을 막지 않는다")
    void post_실패_격리() {
        // Given
        Long gisuId = 9L;
        given(getChallengerUseCase.getAllByGisuId(gisuId)).willReturn(challengers(5));
        AtomicLong postIdSeq = new AtomicLong(100L);
        given(createPostUseCase.createPost(any()))
            .willThrow(new RuntimeException("post boom"))
            .willAnswer(inv -> postInfo(postIdSeq.getAndIncrement()));
        AtomicLong commentIdSeq = new AtomicLong(500L);
        given(createCommentUseCase.create(any()))
            .willAnswer(inv -> commentInfo(commentIdSeq.getAndIncrement()));

        // When - 2 posts, 1 comment per post
        SeedCommunityResult result = sut.seed(new SeedCommunityCommand(gisuId, 2, 1, 0));

        // Then - 1번째 post 실패, 2번째 성공 → comment 는 성공한 post 1개에 대해서만 생성
        assertThat(result.postFailed()).isEqualTo(1);
        assertThat(result.createdPostIds()).hasSize(1);
        assertThat(result.createdCommentIds()).hasSize(1);
        verify(createCommentUseCase, times(1)).create(any());
    }

    @Test
    @DisplayName("commentsPerPost=0 이면 댓글 단계는 스킵된다")
    void comments_0_댓글_미생성() {
        // Given
        Long gisuId = 9L;
        given(getChallengerUseCase.getAllByGisuId(gisuId)).willReturn(challengers(3));
        AtomicLong postIdSeq = new AtomicLong(100L);
        given(createPostUseCase.createPost(any())).willAnswer(inv -> postInfo(postIdSeq.getAndIncrement()));

        // When
        SeedCommunityResult result = sut.seed(new SeedCommunityCommand(gisuId, 2, 0, 0));

        // Then
        assertThat(result.createdPostIds()).hasSize(2);
        assertThat(result.createdCommentIds()).isEmpty();
        verify(createCommentUseCase, never()).create(any());
    }

    @Test
    @DisplayName("gisuId 가 null 이면 활성 기수를 사용한다")
    void gisuId_null_시_활성_기수_조회() {
        // Given
        given(getGisuUseCase.getActiveGisuId()).willReturn(10L);
        given(getChallengerUseCase.getAllByGisuId(10L)).willReturn(List.of());

        // When
        SeedCommunityResult result = sut.seed(new SeedCommunityCommand(null, 1, 1, 1));

        // Then
        assertThat(result.gisuId()).isEqualTo(10L);
        verify(getGisuUseCase, times(1)).getActiveGisuId();
    }

    @Test
    @DisplayName("trophy 만 시딩(post=0, comment=0)도 정상 동작한다")
    void trophy_만_시딩() {
        // Given
        Long gisuId = 9L;
        given(getChallengerUseCase.getAllByGisuId(gisuId)).willReturn(challengers(3));
        AtomicLong trophyIdSeq = new AtomicLong(700L);
        given(createTrophyUseCase.createTrophy(any())).willAnswer(inv -> trophyInfo(trophyIdSeq.getAndIncrement()));

        // When
        SeedCommunityResult result = sut.seed(new SeedCommunityCommand(gisuId, 0, 0, 5));

        // Then
        assertThat(result.createdPostIds()).isEmpty();
        assertThat(result.createdCommentIds()).isEmpty();
        assertThat(result.createdTrophyIds()).hasSize(5);
        verify(createPostUseCase, never()).createPost(any());
        verify(createCommentUseCase, never()).create(any());
        verify(createTrophyUseCase, times(5)).createTrophy(any());
    }

    private void givenIdCountersForCreates() {
        AtomicLong postIdSeq = new AtomicLong(100L);
        AtomicLong commentIdSeq = new AtomicLong(500L);
        AtomicLong trophyIdSeq = new AtomicLong(700L);
        given(createPostUseCase.createPost(any())).willAnswer(inv -> postInfo(postIdSeq.getAndIncrement()));
        given(createCommentUseCase.create(any())).willAnswer(inv -> commentInfo(commentIdSeq.getAndIncrement()));
        given(createTrophyUseCase.createTrophy(any())).willAnswer(inv -> trophyInfo(trophyIdSeq.getAndIncrement()));
    }

    private List<ChallengerInfo> challengers(int count) {
        return java.util.stream.LongStream.range(1, count + 1L)
            .mapToObj(id -> ChallengerInfo.builder()
                .challengerId(id)
                .memberId(id * 10)
                .gisuId(9L)
                .part(ChallengerPart.WEB)
                .challengerStatus(ChallengerStatus.ACTIVE)
                .totalPoints(0.0)
                .build())
            .toList();
    }

    private PostInfo postInfo(long id) {
        return PostInfo.builder().postId(id).build();
    }

    private CommentInfo commentInfo(long id) {
        return CommentInfo.builder().commentId(id).build();
    }

    private TrophyInfo trophyInfo(long id) {
        return new TrophyInfo(id, 1L, 1, null, null, null, null, "t", "c", "u");
    }
}
