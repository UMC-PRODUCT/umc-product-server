package com.umc.product.recruitment.application.service.query;

import com.umc.product.recruitment.application.port.in.query.GetInterviewSchedulingApplicantsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSchedulingAssignmentsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewSchedulingSlotsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyApplicationListUseCase.GetInterviewSchedulingSummaryUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingApplicantsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingAssignmentsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingSlotsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewSchedulingSummaryQuery;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingApplicantsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingAssignmentsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingSlotsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.InterviewSchedulingSummaryInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentInterviewSchedulingQueryService implements GetInterviewSchedulingSummaryUseCase,
        GetInterviewSchedulingSlotsUseCase,
        GetInterviewSchedulingApplicantsUseCase,
        GetInterviewSchedulingAssignmentsUseCase {

    @Override
    public InterviewSchedulingSummaryInfo get(GetInterviewSchedulingSummaryQuery query) {
        // todo: 운영진 권한 검증 필요
        // partOptions: 드롭다운용 파트. done은 해당 날짜 기준으로, ‘해당 파트를 1지망으로 희망하는 지원자들의 면접 시간이 모두 배정되었을 경우’를 의미함
        // rules.timeRange: 운영진이 해당 날짜에 가능한 slot의 시작 시간부터 마지막 시간까지를 의미함
        //      timeRange.end는 마지막 슬롯의 시작 시간이 아닌 종료 시간이라고 생각해주세요!
        return null;
    }

    @Override
    public InterviewSchedulingSlotsInfo get(GetInterviewSchedulingSlotsQuery query) {
        // todo: 운영진 권한 검증 필요
        // 해당 시간 면접 가능 지원자 수는, recruitment.form.formSection.question 중 'SCHEDULE' 타입의 질문에 대한 singleAnswer의 json 값을 파싱해서 계산 필요
        // singleAnswer의 값은
        // {"selected": [{"date": "2026-01-23", "times": ["09:00", "09:30", "10:00"]}, {"date": "2026-01-24", "times": ["09:00", "09:30", "10:00"]}]}
        // 위 형식으로 되어있으며, 이 중 date와 times를 보고 해당 날짜에 가능한 시간대를 파악할 수 있습니다.
        return null;
    }

    @Override
    public InterviewSchedulingApplicantsInfo get(GetInterviewSchedulingApplicantsQuery query) {
        // todo: 운영진 권한 검증 필요
        // 해당 시간 면접 가능 지원자 목록은, recruitment.form.formSection.question 중 'SCHEDULE' 타입의 질문에 대한 singleAnswer의 json 값을 파싱해서 계산 필요
        // singleAnswer의 값은
        // {"selected": [{"date": "2026-01-23", "times": ["09:00", "09:30", "10:00"]}, {"date": "2026-01-24", "times": ["09:00", "09:30", "10:00"]}]}
        // 위 형식으로 되어있으며, 이 중 date와 times를 보고 해당 날짜에 가능한 시간대를 파악할 수 있습니다.

        return null;
    }

    @Override
    public InterviewSchedulingAssignmentsInfo get(GetInterviewSchedulingAssignmentsQuery query) {
        // todo: 운영진 권한 검증 필요
        // 해당 날짜에 면접이 배정된 지원자들의 면접 시간표를 반환

        return null;
    }

}
