package com.umc.product.member.application.port.in.command;

import com.umc.product.member.application.port.in.command.dto.EditBusinessCardCommand;

public interface EditBusinessCardUseCase {

    /**
     * 명함에서 수정할 수 있는 프로필 사진·연락처·링크·공개 여부를 저장합니다. 프로필이 없으면 새로 만듭니다.
     */
    void edit(EditBusinessCardCommand command);
}
