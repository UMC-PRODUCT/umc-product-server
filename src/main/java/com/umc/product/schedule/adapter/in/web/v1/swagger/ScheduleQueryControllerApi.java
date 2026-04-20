package com.umc.product.schedule.adapter.in.web.v1.swagger;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.MyScheduleResponse;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.ScheduleDetailResponse;
import com.umc.product.schedule.adapter.in.web.v1.dto.response.ScheduleListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Schedule V1 | 일정 Query", description = "")
public interface ScheduleQueryControllerApi {

    @Operation(summary = "출석 통계와 함께 일정 목록 조회",
        description = """
            출석 통계와 함께 일정 목록을 조회합니다.
            - 중앙 운영진 : 본인 참석 일정
            - 교내 회장단 : 교내 챌린저가 파트장으로 있는 스터디 그룹 일정 + 본인이 생성한 일정
            - 교내 파트장 : 본인이 파트장으로 있는 스터디 그룹 일정 + 본인이 생성한 일정
            - 기타 운영진 : 본인이 생성한 일정
            """)
    List<ScheduleListResponse> getScheduleList(@CurrentMember MemberPrincipal memberPrincipal);

    @Operation(summary = "월별 내 일정 캘린더/리스트 조회",
        description = "본인이 참여하는 일정을 월 단위로 조회합니다. AttendanceRecord에 등록된 일정만 포함됩니다.")
    List<MyScheduleResponse> getMyScheduleList(
        @CurrentMember MemberPrincipal memberPrincipal,
        @Parameter(description = "연도 (예: 2026)") @RequestParam int year,
        @Parameter(description = "월 (1~12)") @RequestParam int month
    );

    @Operation(summary = "일정 상세 조회",
        description = "특정 일정의 상세 정보를 조회합니다. D-Day, 위치 좌표 등을 포함합니다.")
    ScheduleDetailResponse getScheduleDetail(
        @Parameter(description = "일정 ID") @PathVariable Long scheduleId
    );
}
