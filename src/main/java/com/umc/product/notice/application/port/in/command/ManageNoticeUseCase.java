package com.umc.product.notice.application.port.in.command;

public interface ManageNoticeUseCase {
    /*
    * 공지 임시작성, 다른 요소들(url, img, vote) 추가 후 작성완료 누르면 publish
    * @return 생성된 공지 ID
    * */
    Long createDraftNotice(CreateNoticeCommand command);

    /*
     * 공지 작성 확정
     * 따로 반환값 X
     * */
    void publishNotice(PublishNoticeCommand command);


    /*
    * 공지 수정
    * 따로 반환값 X
    * */
    void updateNotice(UpdateNoticeCommand command);

    /*
    * 공지 삭제
    * 따로 반환값 X
    * */
    void deleteNotice(DeleteNoticeCommand command);

    /*
    * 공지사항 리마인드 알림 보내기
    * */
    void remindNotice(SendNoticeReminderCommand command);




}
