package com.umc.product.organization.adapter.in.web.swagger;

import com.umc.product.schedule.adapter.in.web.v1.dto.request.CreateStudyGroupScheduleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Organization | 스터디 그룹 일정 Command", description = "스터디 그룹 일정을 생성합니다.")
public interface StudyGroupScheduleControllerApi {

    @Operation(summary = "스터디 그룹 일정 생성", description = """
        스터디 그룹 일정을 생성합니다.
        **'Schedule V2 | Command'**의 **'일정 생성 API'**를 사용하신 후 이 API를 호출해주세요.

        > ⚠️ **주의 : ** 이 API를 사용하기 전, '일정 생성 API'를 사용하실 때는 사용자 본인을 무조건 참여자로 넣는 로직은 생략해주세요.
        """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = """
            ~~-00 : ~~.<br>
             """,
            content = @Content
        )
    })
    Long create(CreateStudyGroupScheduleRequest request);
}
