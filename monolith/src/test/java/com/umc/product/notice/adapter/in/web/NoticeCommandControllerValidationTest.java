package com.umc.product.notice.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.support.ControllerTestSupport;

@DisplayName("공지 생성·수정 제목과 본문 입력 검증")
class NoticeCommandControllerValidationTest extends ControllerTestSupport {

    private static final Long NOTICE_ID = 42L;
    private static final NoticeTargetInfo TARGET_INFO =
        new NoticeTargetInfo(9L, null, null, List.of(), NoticeTab.CHALLENGER);

    @ParameterizedTest
    @CsvSource({
        "CREATE, 255, 3000",
        "CREATE, 254, 2999",
        "UPDATE, 255, 3000",
        "UPDATE, 254, 2999"
    })
    @DisplayName("최대 길이와 그 이하의 제목·본문은 생성과 수정 서비스에 그대로 전달된다")
    void 허용된_길이_요청_전달(Operation operation, int titleLength, int contentLength) throws Exception {
        // Given
        String title = text(titleLength);
        String content = text(contentLength);
        if (operation == Operation.CREATE) {
            given(manageNoticeUseCase.createNotice(any())).willReturn(NOTICE_ID);
        }

        // When
        mockMvc.perform(request(operation, title, content))
            .andExpect(status().isOk());

        // Then
        if (operation == Operation.CREATE) {
            verify(manageNoticeUseCase).createNotice(
                new CreateNoticeCommand(TEST_MEMBER_ID, title, content, false, false, TARGET_INFO));
        } else {
            verify(manageNoticeUseCase).updateNoticeTitleOrContent(
                new UpdateNoticeCommand(TEST_MEMBER_ID, NOTICE_ID, title, content, false));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "CREATE, 256, 3000, title",
        "CREATE, 255, 3001, content",
        "UPDATE, 256, 3000, title",
        "UPDATE, 255, 3001, content",
        "CREATE, 0, 3000, title",
        "CREATE, 255, 0, content",
        "UPDATE, 0, 3000, title",
        "UPDATE, 255, 0, content",
        "CREATE, -1, 3000, title",
        "CREATE, 255, -1, content",
        "UPDATE, -1, 3000, title",
        "UPDATE, 255, -1, content"
    })
    @DisplayName("길이 초과·빈 값·누락된 제목이나 본문은 400으로 거부하고 서비스를 호출하지 않는다")
    void 잘못된_입력_요청_거부(Operation operation, int titleLength, int contentLength,
                            String invalidField) throws Exception {
        // Given
        String title = text(titleLength);
        String content = text(contentLength);

        // When
        mockMvc.perform(request(operation, title, content))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("COMMON-400"))
            .andExpect(jsonPath("$.result." + invalidField).isNotEmpty());

        // Then
        verifyNoInteractions(manageNoticeUseCase);
    }

    private MockHttpServletRequestBuilder request(Operation operation, String title, String content) throws Exception {
        MockHttpServletRequestBuilder builder = switch (operation) {
            case CREATE -> post("/api/v1/notices").content(objectMapper.writeValueAsString(
                new CreateNoticeRequest(title, content, false, false, TARGET_INFO)));
            case UPDATE -> patch("/api/v1/notices/{noticeId}", NOTICE_ID).content(objectMapper.writeValueAsString(
                new UpdateNoticeRequest(title, content, false)));
        };
        return builder.contentType(MediaType.APPLICATION_JSON);
    }

    private String text(int length) {
        return length < 0 ? null : "가".repeat(length);
    }

    enum Operation {
        CREATE,
        UPDATE
    }
}
