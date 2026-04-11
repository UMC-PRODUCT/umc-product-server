package com.umc.product.schedule.adapter.in.web.v2;

import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.schedule.adapter.in.web.v2.dto.request.CreateScheduleRequest;
import com.umc.product.schedule.adapter.in.web.v2.dto.request.EditScheduleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule V2 | Command", description = "일정을 생성하거나 수정하고, 출석 관련 요청을 처리합니다.")
public class ScheduleCommandController {

    @Operation(summary = "일정 생성", description = """
        일정을 생성합니다. `location` 필드를 작성하지 않으실 경우 비대면 일정으로 간주됩니다.

        스터디 일정을 생성하고자 하는 경우에는, 반드시 별도의 API를 이용해서 생성해야 합니다.
        그렇지 않은 경우, 스터디 미진행으로 인한 벌점이 부과될 수 있습니다.

        하루종일 일정의 경우, 클라이언트 단에서 KST 기준으로 시작일 00:00 ~ 종료일 00:00 을 UTC-Based ISO8601 형식으로 보내주셔야 합니다.
        조회할 때도 반대로 KST 기준 00:00에 시작해서 23:59에 끝나는 일정의 경우에 자동으로 하루 종일로 표시해주시면 됩니다.

        일정 생성 시, 초대 가능한 챌린저 수에 제한이 존재합니다.
        일반 챌린저: 50명, 회장단: 100명, 지부장: 300명, 총괄단: 2,000명

        (생성 예정) 일정 생성 시 참여자 목록을 기반으로 해당 인원들이 참여하는 스터디 그룹이 있는지를
        검사하는 API를 토대로 사용자에게 경고 등을 띄울 것을 추천드립니다.

        생성된 일정의 ID 값을 반환합니다.
        """
    )
    @PostMapping
    public Long create(CreateScheduleRequest request) {
        throw new NotImplementedException();
    }

    @Operation(summary = "일정 수정", description = """
        일정과 관련된 모든 정보를 수정합니다. 제공되지 않은 필드는 변경하지 않는 것으로 간주합니다.
        """
    )
    @PatchMapping("/{scheduleId}")
    public void edit(
        @PathVariable String scheduleId,
        EditScheduleRequest request
    ) {
        throw new NotImplementedException();
    }

    // 각 일정에 출석 요청을 보내는 API -> 변경된 상태는 조회 API를 호출하여 상태를 조회하기 바랍니다.

    // 각 일정에 대한 출석 요청을 승인 또는 기각하는 API, Request는 list 형태로 받을 수 있어야 합니다.

    // TODO: 일정 신고 API (본인이 참여하지 않는 일정에 강제로 초대당한 경우)

}
