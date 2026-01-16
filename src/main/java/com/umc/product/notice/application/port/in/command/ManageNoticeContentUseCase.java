package com.umc.product.notice.application.port.in.command;

import com.umc.product.notice.application.port.in.command.dto.AddNoticeImagesCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeLinksCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeVotesCommand;
import com.umc.product.notice.application.port.in.command.dto.RemoveNoticeImageCommand;
import com.umc.product.notice.application.port.in.command.dto.RemoveNoticeLinkCommand;
import com.umc.product.notice.application.port.in.command.dto.RemoveNoticeVoteCommand;
import java.util.List;

/*
 * 공지의 본체 외 이미지, 투표, 링크 관리
 */
public interface ManageNoticeContentUseCase {

    /*
     * 공지에 투표 추가
     * @return 생성된 NoticeVote의 id 리스트
     */
    List<Long> addVotes(AddNoticeVotesCommand command, Long noticeId);

    /*
     * 공지에 이미지 추가
     * @return 생성된 NoticeImage의 id 리스트
     */
    List<Long> addImages(AddNoticeImagesCommand command, Long noticeId);

    /*
     * 공지에 링크 추가
     * @return 생성된 NoticeLink의 id 리스트
     */
    List<Long> addLinks(AddNoticeLinksCommand command, Long noticeId);

    /*
     * 공지에서 투표 삭제
     * @return
     */
    void removeVote(RemoveNoticeVoteCommand command);

    /*
     * 공지에서 이미지 삭제
     * @return
     */
    void removeImage(RemoveNoticeImageCommand command);

    /*
     * 공지에서 링크 삭제
     * @return
     */
    void removeLink(RemoveNoticeLinkCommand command);
}
