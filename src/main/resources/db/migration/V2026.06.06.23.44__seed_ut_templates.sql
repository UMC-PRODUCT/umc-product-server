-- 사용자 피드백 템플릿 초기 시딩 (10기 기준 5개 템플릿)
-- created_member_id = 0: 시스템 생성 폼 (FK 제약 없음)
-- is_active = true: 현재 기수 활성 템플릿

DO
$$
    DECLARE
        v_form_id    BIGINT;
        v_section_id BIGINT;
        v_q1_id      BIGINT;
        v_q2_id      BIGINT;
        v_q3_id      BIGINT;
        v_q4_id      BIGINT;
        v_q5_id      BIGINT;

    BEGIN

        -- =====================================================================
        -- Template 1: APPLICATION_SUBMITTED + NEW_CHALLENGER
        -- 대상: NEW 챌린저 (Design, FE, BE)
        -- 상황: 지원서 작성 완료 후 [돌아가기] 클릭 시
        -- =====================================================================
        INSERT INTO form (created_member_id, title, status, is_anonymous, created_at, updated_at)
        VALUES (0, '프로젝트 지원 경험이 어떠셨나요?', 'PUBLISHED', false, NOW(), NOW())
        RETURNING id INTO v_form_id;

        INSERT INTO form_section (form_id, title, order_no, created_at, updated_at)
        VALUES (v_form_id, '사용자 조사', 1, NOW(), NOW())
        RETURNING id INTO v_section_id;

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '사용하기에는 어려웠나요?', 'RADIO', true, 1, true, NOW(), NOW())
        RETURNING id INTO v_q1_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '쉬움', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 쉬움', 2, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '보통', 3, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 어려움', 4, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '어려움', 5, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '프로젝트 탐색과 지원 과정은 어떠셨나요?', 'RADIO', true, 2, true, NOW(), NOW())
        RETURNING id INTO v_q2_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '만족스러워요', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '아쉬워요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '가장 불편했던 점 or 좋았던 점은 무엇이었나요?', 'LONG_TEXT', false, 3, true, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '추가로 남기고 싶은 의견이 있나요?', 'LONG_TEXT', false, 4, true, NOW(), NOW());

        INSERT INTO user_feedback_template (context, target_type, form_id, is_active, created_at, updated_at)
        VALUES ('APPLICATION_SUBMITTED', 'NEW_CHALLENGER', v_form_id, true, NOW(), NOW());


        -- =====================================================================
        -- Template 2: APPLICATION_SUBMITTED + EXPERIENCED_CHALLENGER
        -- 대상: 이전 기수 경력의 챌린저 (Design, FE, BE)
        -- 상황: 지원서 작성 완료 후 [돌아가기] 클릭 시
        -- =====================================================================
        INSERT INTO form (created_member_id, title, status, is_anonymous, created_at, updated_at)
        VALUES (0, '새롭게 바뀐 지원 경험이 어떠셨나요?', 'PUBLISHED', false, NOW(), NOW())
        RETURNING id INTO v_form_id;

        INSERT INTO form_section (form_id, title, order_no, created_at, updated_at)
        VALUES (v_form_id, '사용자 조사', 1, NOW(), NOW())
        RETURNING id INTO v_section_id;

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '사용하기에는 어려웠나요?', 'RADIO', true, 1, true, NOW(), NOW())
        RETURNING id INTO v_q1_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '쉬움', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 쉬움', 2, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '보통', 3, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 어려움', 4, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '어려움', 5, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '프로젝트 탐색과 지원 과정은 어떠셨나요?', 'RADIO', true, 2, true, NOW(), NOW())
        RETURNING id INTO v_q2_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '전보다 편해요', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '전보다 불편해요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '가장 불편했던 점 or 좋았던 점은 무엇이었나요?', 'LONG_TEXT', false, 3, true, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '추가로 남기고 싶은 의견이 있나요?', 'LONG_TEXT', false, 4, true, NOW(), NOW());

        INSERT INTO user_feedback_template (context, target_type, form_id, is_active, created_at, updated_at)
        VALUES ('APPLICATION_SUBMITTED', 'EXPERIENCED_CHALLENGER', v_form_id, true, NOW(), NOW());


        -- =====================================================================
        -- Template 3: MATCHING_COMPLETED + NEW_CHALLENGER
        -- 대상: NEW 챌린저 (Design, FE, BE)
        -- 상황: 매칭 종료 후 매칭 현황 페이지 접속 (본인 결과 확인 후) 30초 후
        -- =====================================================================
        INSERT INTO form (created_member_id, title, status, is_anonymous, created_at, updated_at)
        VALUES (0, '전반적인 매칭 경험이 어떠셨나요?', 'PUBLISHED', false, NOW(), NOW())
        RETURNING id INTO v_form_id;

        INSERT INTO form_section (form_id, title, order_no, created_at, updated_at)
        VALUES (v_form_id, '사용자 조사', 1, NOW(), NOW())
        RETURNING id INTO v_section_id;

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '사용하기에는 어려웠나요?', 'RADIO', true, 1, true, NOW(), NOW())
        RETURNING id INTO v_q1_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '쉬움', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 쉬움', 2, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '보통', 3, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 어려움', 4, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '어려움', 5, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '이번 매칭 과정은 전반적으로 어떠셨나요?', 'RADIO', true, 2, true, NOW(), NOW())
        RETURNING id INTO v_q2_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '만족스러워요', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '아쉬워요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '다음에도 이 방식으로 이용하고 싶으신가요?', 'RADIO', true, 3, true, NOW(), NOW())
        RETURNING id INTO v_q3_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q3_id, '좋아요', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q3_id, '아니요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '가장 불편했던 점 or 좋았던 점은 무엇이었나요?', 'LONG_TEXT', false, 4, true, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '추가로 남기고 싶은 의견이 있나요?', 'LONG_TEXT', false, 5, true, NOW(), NOW());

        INSERT INTO user_feedback_template (context, target_type, form_id, is_active, created_at, updated_at)
        VALUES ('MATCHING_COMPLETED', 'NEW_CHALLENGER', v_form_id, true, NOW(), NOW());


        -- =====================================================================
        -- Template 4: MATCHING_COMPLETED + EXPERIENCED_CHALLENGER
        -- 대상: 이전 기수 경력의 챌린저 (PM, Design, FE, BE)
        -- 상황: 매칭 종료 후 매칭 현황 페이지 접속 (본인 결과 확인 후) 30초 후
        -- =====================================================================
        INSERT INTO form (created_member_id, title, status, is_anonymous, created_at, updated_at)
        VALUES (0, '새롭게 바뀐 매칭 경험이 어떠셨나요?', 'PUBLISHED', false, NOW(), NOW())
        RETURNING id INTO v_form_id;

        INSERT INTO form_section (form_id, title, order_no, created_at, updated_at)
        VALUES (v_form_id, '사용자 조사', 1, NOW(), NOW())
        RETURNING id INTO v_section_id;

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '사용하기에는 어려웠나요?', 'RADIO', true, 1, true, NOW(), NOW())
        RETURNING id INTO v_q1_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '쉬움', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 쉬움', 2, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '보통', 3, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 어려움', 4, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '어려움', 5, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '이번 매칭 과정은 전반적으로 어떠셨나요?', 'RADIO', true, 2, true, NOW(), NOW())
        RETURNING id INTO v_q2_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '전보다 편해요', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '전보다 불편해요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '다음에도 이 방식으로 이용하고 싶으신가요?', 'RADIO', true, 3, true, NOW(), NOW())
        RETURNING id INTO v_q3_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q3_id, '좋아요', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q3_id, '아니요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '가장 불편했던 점 or 좋았던 점은 무엇이었나요?', 'LONG_TEXT', false, 4, true, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '추가로 남기고 싶은 의견이 있나요?', 'LONG_TEXT', false, 5, true, NOW(), NOW());

        INSERT INTO user_feedback_template (context, target_type, form_id, is_active, created_at, updated_at)
        VALUES ('MATCHING_COMPLETED', 'EXPERIENCED_CHALLENGER', v_form_id, true, NOW(), NOW());


        -- =====================================================================
        -- Template 5: APPLICATION_MONITORING + ADMIN
        -- 대상: 중앙 운영진, 어드민
        -- 상황: 지원 현황 3차까지 종료 후 접속 1분 뒤
        -- =====================================================================
        INSERT INTO form (created_member_id, title, status, is_anonymous, created_at, updated_at)
        VALUES (0, '새롭게 바뀐 매칭 경험이 어떠셨나요?', 'PUBLISHED', false, NOW(), NOW())
        RETURNING id INTO v_form_id;

        INSERT INTO form_section (form_id, title, order_no, created_at, updated_at)
        VALUES (v_form_id, '사용자 조사', 1, NOW(), NOW())
        RETURNING id INTO v_section_id;

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '사용하기에는 어려웠나요?', 'RADIO', true, 1, true, NOW(), NOW())
        RETURNING id INTO v_q1_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '쉬움', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 쉬움', 2, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '보통', 3, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '조금 어려움', 4, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q1_id, '어려움', 5, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '이번 매칭 과정은 전반적으로 어떠셨나요?', 'RADIO', true, 2, true, NOW(), NOW())
        RETURNING id INTO v_q2_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '전보다 편해요', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q2_id, '전보다 불편해요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '필요한 정보를 찾는 데 어려움이 있었나요?', 'RADIO', true, 3, true, NOW(), NOW())
        RETURNING id INTO v_q3_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q3_id, '전혀 없었어요', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q3_id, '조금 있었어요', 2, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q3_id, '많이 있었어요', 3, false, NOW(), NOW());

        -- Q3 조건부: 프론트에서 '조금 있었어요' / '많이 있었어요' 선택 시 표시
        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '어떤 점이 어려우셨나요?', 'LONG_TEXT', false, 4, true, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '다음에도 이 방식으로 이용하고 싶으신가요?', 'RADIO', true, 5, true, NOW(), NOW())
        RETURNING id INTO v_q4_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q4_id, '좋아요', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q4_id, '아니요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '불편했던 점 or 좋았던 점은 무엇이었나요?', 'LONG_TEXT', false, 6, true, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '가장 불편했던 단계는 어디였나요?', 'RADIO', false, 7, true, NOW(), NOW())
        RETURNING id INTO v_q5_id;
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q5_id, '공지 작성', 1, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q5_id, '프로젝트 목록 및 정보 확인', 2, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q5_id, '지원 현황', 3, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q5_id, '매칭 현황', 4, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q5_id, '매칭 차수 설정', 5, false, NOW(), NOW());
        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at) VALUES (v_q5_id, '기타', 6, true, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '추가로 남기고 싶은 의견이 있나요?', 'LONG_TEXT', false, 8, true, NOW(), NOW());

        INSERT INTO user_feedback_template (context, target_type, form_id, is_active, created_at, updated_at)
        VALUES ('APPLICATION_MONITORING', 'ADMIN', v_form_id, true, NOW(), NOW());

    END
$$;
