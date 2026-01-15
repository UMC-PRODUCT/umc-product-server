package com.umc.product.notice.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = Constants.NOTICE)
public class NoticeController {

    private final ManageNoticeUseCase manageNoticeUseCase;

    public void createNotice(@RequestBody @Valid CreateNoticeRequest request) {

    }


    /*
     * 조회 관련 api
     */


}
