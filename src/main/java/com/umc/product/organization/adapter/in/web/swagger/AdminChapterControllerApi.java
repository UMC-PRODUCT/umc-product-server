package com.umc.product.organization.adapter.in.web.swagger;

import com.umc.product.organization.adapter.in.web.dto.request.CreateChapterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Organization | 지부 Command", description = "")
public interface AdminChapterControllerApi {

    @Operation(summary = "지부 생성", description = "새로운 지부를 생성합니다. 소속 학교를 함께 지정할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "기수 또는 학교를 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "지부명 중복 또는 학교가 이미 다른 지부에 배정됨")
    })
    Long createChapter(@Valid CreateChapterRequest request);

    @Operation(summary = "지부 일괄 생성")
    List<Long> createChapterBulk(@RequestBody List<CreateChapterRequest> requests);

    @Operation(summary = "지부 삭제", description = "지부를 삭제합니다. 소속 학교는 삭제되지 않습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "지부를 찾을 수 없음")
    })
    void deleteChapter(@Parameter(description = "지부 ID", required = true) Long chapterId);
}
