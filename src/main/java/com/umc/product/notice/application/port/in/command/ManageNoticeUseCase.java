package com.umc.product.notice.application.port.in.command;

import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.PublishNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;

public interface ManageNoticeUseCase {
    /*
    * 공지 임시작성, 다른 요소들(url, img, vote) 추가 후 작성완료 누르면 publish
    * @return 생성된 공지 ID
    */
    Long createDraftNotice(CreateNoticeCommand command);

    /*
     * 공지 작성 확정
     * @return
     */
    void publishNotice(PublishNoticeCommand command);


    /*
    * 공지 수정
    * @return
    */
    void updateNotice(UpdateNoticeCommand command);

    /*
    * 공지 삭제
    * @return
    */
    void deleteNotice(DeleteNoticeCommand command);

    /*
    * 공지사항 리마인드 알림 보내기
    */
    void remindNotice(SendNoticeReminderCommand command);




}
