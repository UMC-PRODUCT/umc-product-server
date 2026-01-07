package com.umc.product.schedule.application.port.in.attendance;

import com.umc.product.schedule.domain.AttendanceRecord.AttendanceRecordId;
import java.util.List;

//출석 자체에 대한 기능 구현.
public interface AttendanceRecordUseCase {

    // 출석체크
    AttendanceRecordId checkAttendance(CheckAttendanceCommand command);

    //출석 승인
    void approveAttendance(AttendanceRecordId recordId);

    //출석 거부
    void rejectAttendance(AttendanceRecordId recordId);

    //출석 기록 하나만 가져오는거
    AttendanceRecordResponse getAttendanceRecord(AttendanceRecordId recordId);

    //출석 목록 가져오기
    List<AttendanceRecordResponse> getAttendanceRecordsBySheet(Long sheetId);

    //챌린저 별로 가져오기
    List<AttendanceRecordResponse> getAttendanceRecordsByChallenger(Long challengerId);

    //대기하는 출석들 목록
    List<AttendanceRecordResponse> getPendingRecords(Long sheetId);
}
