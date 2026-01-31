package com.umc.product.notice.application.port.in.command;

import static org.junit.jupiter.api.Assertions.*;

import com.umc.product.notice.application.port.out.LoadNoticeImagePort;
import com.umc.product.notice.application.port.out.LoadNoticeLinkPort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.application.port.out.SaveNoticeImagePort;
import com.umc.product.notice.application.port.out.SaveNoticeLinkPort;
import com.umc.product.notice.application.port.out.SaveNoticeVotePort;
import com.umc.product.support.UseCaseTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ManageNoticeContentUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ManageNoticeContentUseCase manageNoticeContentUseCase;

    @Autowired
    private LoadNoticeVotePort loadNoticeVotePort;

    @Autowired
    private LoadNoticeLinkPort loadNoticeLinkPort;

    @Autowired
    private LoadNoticeImagePort loadNoticeImagePort;

    @Autowired
    private SaveNoticeVotePort saveNoticeVotePort;

    @Autowired
    private SaveNoticeImagePort saveNoticeImagePort;

    @Autowired
    private SaveNoticeLinkPort saveNoticeLinkPort;

    
    @Test
    void addVotes() {
    }
}
