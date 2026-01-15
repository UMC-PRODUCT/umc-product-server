package com.umc.product.notice.application.service.command;

import com.umc.product.common.dto.ChallengerContext;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeImagesCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeLinksCommand;
import com.umc.product.notice.application.port.in.command.dto.AddNoticeVotesCommand;
import com.umc.product.notice.application.port.in.command.dto.RemoveNoticeImageCommand;
import com.umc.product.notice.application.port.in.command.dto.RemoveNoticeLinkCommand;
import com.umc.product.notice.application.port.in.command.dto.RemoveNoticeVoteCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeContentService implements ManageNoticeContentUseCase {
    @Override
    public List<Long> addVotes(AddNoticeVotesCommand command, ChallengerContext context) {
        return null;
    }

    @Override
    public List<Long> addImages(AddNoticeImagesCommand command, ChallengerContext context) {
        return null;
    }

    @Override
    public List<Long> addLinks(AddNoticeLinksCommand command, ChallengerContext context) {
        return null;
    }

    @Override
    public void removeVote(RemoveNoticeVoteCommand command, ChallengerContext context) {

    }

    @Override
    public void removeImage(RemoveNoticeImageCommand command, ChallengerContext context) {

    }

    @Override
    public void removeLink(RemoveNoticeLinkCommand command, ChallengerContext context) {

    }
}
