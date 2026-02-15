package com.umc.product.global.response;

import java.util.List;
import java.util.function.Function;

/**
 * 커서 기반 페이지네이션 응답을 표현하는 공통 레코드입니다.
 *
 * @param content    현재 페이지의 데이터 목록
 * @param nextCursor 다음 페이지 조회 시 사용할 커서 값 (다음 페이지가 없으면 {@code null})
 * @param hasNext    다음 페이지 존재 여부
 * @param <T>        응답 데이터 타입
 */
public record CursorResponse<T>(List<T> content, Long nextCursor, boolean hasNext) {

    /**
     * 이미 계산된 값들로 커서 응답을 직접 생성합니다.
     *
     * <p>nextCursor와 hasNext를 호출부에서 직접 계산한 경우에 사용합니다.
     *
     * <pre>{@code
     * // Service에서 이미 커서 정보를 계산한 경우
     * CursorResponse.of(challengerList, lastId, hasMoreData);
     * }</pre>
     *
     * @param content    현재 페이지의 데이터 목록
     * @param nextCursor 다음 페이지 커서 값 (없으면 {@code null})
     * @param hasNext    다음 페이지 존재 여부
     */
    public static <T> CursorResponse<T> of(List<T> content, Long nextCursor, boolean hasNext) {
        return new CursorResponse<>(content, nextCursor, hasNext);
    }

    /**
     * 조회 결과로부터 커서 정보를 자동 계산하고, 데이터를 변환하여 응답을 생성합니다.
     *
     * <p>Repository에서 {@code requestedSize + 1}개를 조회한 결과를 그대로 넘기면,
     * 이 메서드가 다음 페이지 존재 여부 판단, 초과분 제거, 커서 추출, 타입 변환을 모두 처리합니다.
     *
     * <pre>{@code
     * // Repository: LIMIT size + 1 로 조회
     * List<Challenger> challengers = repository.cursorSearch(query, cursor, size);
     *
     * // size=20으로 요청했는데 21개가 왔으면 → hasNext=true, 마지막 1개 제거
     * // 20개 이하가 왔으면 → hasNext=false, 그대로 사용
     * CursorResponse<ChallengerItemResponse> response = CursorResponse.of(
     *     challengers,                       // size + 1개를 조회한 원본 리스트
     *     size,                              // 요청한 페이지 크기 (20)
     *     Challenger::getId,                 // 마지막 요소에서 커서 값 추출
     *     ChallengerItemResponse::from       // Challenger → Response 변환
     * );
     * }</pre>
     *
     * @param content         {@code requestedSize + 1}개까지 조회된 원본 리스트
     * @param requestedSize   요청한 페이지 크기 (실제 반환할 최대 개수)
     * @param cursorExtractor 마지막 요소에서 다음 커서 값을 추출하는 함수
     * @param mapper          원본 타입 {@code T}를 응답 타입 {@code R}로 변환하는 함수
     * @param <T>             원본 데이터 타입
     * @param <R>             변환된 응답 데이터 타입
     */
    public static <T, R> CursorResponse<R> of(List<T> content, int requestedSize, Function<T, Long> cursorExtractor,
                                              Function<T, R> mapper) {
        boolean hasNext = content.size() > requestedSize;

        List<T> result = hasNext ? content.subList(0, requestedSize) : content;

        List<R> mappedContent = result.stream().map(mapper).toList();

        Long nextCursor = hasNext && !result.isEmpty() ? cursorExtractor.apply(result.get(result.size() - 1)) : null;

        return new CursorResponse<>(mappedContent, nextCursor, hasNext);
    }

    /**
     * 빈 커서 응답을 생성합니다.
     *
     * <p>검색 결과가 없거나 조기 반환이 필요한 경우에 사용합니다.
     *
     * <pre>{@code
     * if (query.gisuId() == null) {
     *     return CursorResponse.empty();
     * }
     * }</pre>
     */
    public static <T> CursorResponse<T> empty() {
        return new CursorResponse<>(List.of(), null, false);
    }
}

