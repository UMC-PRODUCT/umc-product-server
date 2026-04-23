-- schedule_participant 테이블에 attendance_sheet & attendance_record 값 이관

INSERT INTO public.schedule_participant (id,
                                         member_id,
                                         schedule_id,
                                         location,
                                         attendance_status,
                                         decided_by_member_id,
                                         decided_at,
                                         decision_reason,
                                         is_location_verified,
                                         excuse_reason,
                                         created_at,
                                         updated_at)
SELECT ar.id,
       ar.member_id,
       ash.schedule_id,
       CASE
           WHEN ar.latitude IS NOT NULL AND ar.longitude IS NOT NULL
               THEN ST_SetSRID(ST_MakePoint(ar.longitude, ar.latitude), 4326)
           ELSE NULL
           END,
       CASE
           WHEN ar.status = 'PENDING' THEN NULL
           ELSE ar.status
           END,
       ar.confirmed_by,
       ar.confirmed_at,
       NULL,
       ar.location_verified,
       ar.memo,
       ar.created_at,
       ar.updated_at
FROM public.attendance_record ar
         JOIN public.attendance_sheet ash ON ar.attendance_sheet_id = ash.id;

-- 시퀀스를 이관된 데이터의 max id 이후부터 시작하도록 설정
SELECT setval(
           'public.schedule_participant_id_seq',
           COALESCE((SELECT MAX(id) FROM public.schedule_participant), 0) + 1,
           false
       );
