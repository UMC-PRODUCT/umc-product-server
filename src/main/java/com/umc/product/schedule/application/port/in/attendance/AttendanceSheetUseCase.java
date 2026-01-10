package com.umc.product.schedule.application.port.in.attendance;

import com.umc.product.schedule.domain.AttendanceSheet.AttendanceSheetId;
import java.util.List;

//출석 부에 맞춘 기능 구현
public interface AttendanceSheetUseCase {

    //출석부
    AttendanceSheetId createAttendanceSheet(CreateAttendanceSheetCommand command);

    //출석부 수정
    void updateAttendanceSheet(UpdateAttendanceSheetCommand command);

    //출석부 비활성화(삭제가 나은것가기ㄷ하고)
    void deleteAttendanceSheet(AttendanceSheetId sheetId);

    //출석부 하나만 조회
    AttendanceSheetResponse getAttendanceSheet(AttendanceSheetId sheetId);

    //일정 별로 조회하기, 일정 코드 받아오면 수정 해야함
    AttendanceSheetResponse getAttendanceSheetBySchedule(Long scheduleId);

    //출석부 목록조회로 위와 경우 같음
    List<AttendanceSheetResponse> getAttendanceSheets(List<Long> scheduleIds);
}
