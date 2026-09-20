package com.umc.product.test.application.port.in.command;

import com.umc.product.test.application.port.in.command.dto.SeedNoticeCommand;
import com.umc.product.test.application.port.in.command.dto.SeedNoticeResult;

/**
 * 활성 기수(또는 지정 기수)에 대해 공지사항을 여러 지부·학교·파트에 걸쳐 분포 시딩한다.
 * 제목·내용에 대상 범위 정보를 포함시켜 운영 화면에서 식별이 쉬워지도록 한다. ADR-017 참조.
 */
public interface SeedNoticeUseCase {

    SeedNoticeResult seed(SeedNoticeCommand command);
}
