package com.umc.product.chat.domain;

/**
 * 채팅 메시지의 콘텐츠 종류.
 * <p>
 * 발신자 역할(운영진/문의자 등)이 아니라 "메시지 내용이 무엇인가"를 구분한다.
 * <ul>
 *     <li>{@code TEXT} — 일반 텍스트 메시지.</li>
 *     <li>{@code IMAGE} — 이미지 첨부 (content는 캡션, fileMetadataIds에 이미지 참조).</li>
 *     <li>{@code FILE} — 파일 첨부 (content는 캡션, fileMetadataIds에 파일 참조).</li>
 *     <li>{@code SYSTEM} — 자동 안내 또는 삭제 tombstone 메시지. tombstone은 원 발신자를 보존한다.</li>
 *     <li>{@code MEMBER_LEFT} — 멤버가 채팅방을 나갔음을 알리는 기록. sender는 나간 멤버다.</li>
 *     <li>{@code MEMBER_KICKED} — 멤버가 채팅방에서 내보내졌음을 알리는 기록. sender는 내보내진 멤버다.</li>
 * </ul>
 */
public enum MessageContentType {
    TEXT,
    IMAGE,
    FILE,
    SYSTEM,
    MEMBER_LEFT,
    MEMBER_KICKED;

    /**
     * 서버 내부에서만 생성하는 메시지인지 여부. 클라이언트 전송, 수정, 리액션 대상이 아니다.
     */
    public boolean isServerGenerated() {
        return this == SYSTEM || this == MEMBER_LEFT || this == MEMBER_KICKED;
    }

    /**
     * 멤버 퇴장(나가기/내보내기) 기록 메시지인지 여부.
     */
    public boolean isMemberExit() {
        return this == MEMBER_LEFT || this == MEMBER_KICKED;
    }
}
