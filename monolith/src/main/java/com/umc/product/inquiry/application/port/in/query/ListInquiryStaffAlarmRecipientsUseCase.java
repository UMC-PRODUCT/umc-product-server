package com.umc.product.inquiry.application.port.in.query;

import java.util.List;

public interface ListInquiryStaffAlarmRecipientsUseCase {

    /**
     * 작성자 쪽 메시지를 알릴 운영진: {@code UMC_PRODUCT} 회원 중 알림을 끄지 않은 사람에서 보낸 사람을 뺀다.
     */
    List<Long> listRecipientMemberIds(Long senderMemberId);
}
