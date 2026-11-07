-- 기존 Form의 상태나 내용은 변경하지 않는다. 문항·선택지·응답도 보존한다.
-- 지원·매칭 설문의 신규·기존 전용 user_feedback_template만 is_active=false로 변경한다.
-- 이후 각 context에 새 Form과 CHALLENGER 공통 템플릿을 하나씩 생성한다.
-- 운영진 설문(APPLICATION_MONITORING / ADMIN)은 변경하지 않는다.
UPDATE user_feedback_template
SET is_active = false,
    updated_at = NOW()
WHERE is_active = true
  AND context IN ('APPLICATION_SUBMITTED', 'MATCHING_COMPLETED')
  AND target_type IN ('NEW_CHALLENGER', 'EXPERIENCED_CHALLENGER');

DO
$$
    DECLARE
        v_form_id     BIGINT;
        v_section_id  BIGINT;
        v_question_id BIGINT;
    BEGIN
        -- 지원과정: 신규·기존 챌린저가 공유하는 새 폼
        INSERT INTO form (created_member_id, title, status, is_anonymous, allow_duplicate_responses, created_at, updated_at)
        VALUES (0, '프로젝트 지원 경험이 어떠셨나요?', 'PUBLISHED', false, false, NOW(), NOW())
        RETURNING id INTO v_form_id;

        INSERT INTO form_section (form_id, title, order_no, created_at, updated_at)
        VALUES (v_form_id, '사용자 조사', 1, NOW(), NOW())
        RETURNING id INTO v_section_id;

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '프로젝트를 지원하는 과정은 어떠셨나요?', 'RADIO', true, 1, true, NOW(), NOW())
        RETURNING id INTO v_question_id;

        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at)
        VALUES (v_question_id, '어려워요', 1, false, NOW(), NOW()),
               (v_question_id, '조금 어려워요', 2, false, NOW(), NOW()),
               (v_question_id, '보통이에요', 3, false, NOW(), NOW()),
               (v_question_id, '조금 쉬워요', 4, false, NOW(), NOW()),
               (v_question_id, '쉬워요', 5, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '새로워진 UPMS의 화면 경험은 어떠셨나요?', 'RADIO', true, 2, true, NOW(), NOW())
        RETURNING id INTO v_question_id;

        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at)
        VALUES (v_question_id, '전보다 불편해요', 1, false, NOW(), NOW()),
               (v_question_id, '전보다 편해요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '지원할 때 가장 유용했던 기능은 무엇인가요?', 'RADIO', true, 3, true, NOW(), NOW())
        RETURNING id INTO v_question_id;

        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at)
        VALUES (v_question_id, '프로젝트 검색 및 필터', 1, false, NOW(), NOW()),
               (v_question_id, '프로젝트 상세 정보 확인', 2, false, NOW(), NOW()),
               (v_question_id, '지원서 작성 및 수정', 3, false, NOW(), NOW()),
               (v_question_id, '지원 현황 확인', 4, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '가장 좋았거나 아쉬웠던 점은 무엇인가요? (선택)', 'LONG_TEXT', false, 4, true, NOW(), NOW()),
               (v_section_id, '추가로 남기고 싶은 의견이 있나요? (선택)', 'LONG_TEXT', false, 5, true, NOW(), NOW());

        INSERT INTO user_feedback_template (context, target_type, form_id, is_active, created_at, updated_at)
        VALUES ('APPLICATION_SUBMITTED', 'CHALLENGER', v_form_id, true, NOW(), NOW());

        -- 매칭과정: 지원 설문과 별도의 새 폼
        INSERT INTO form (created_member_id, title, status, is_anonymous, allow_duplicate_responses, created_at, updated_at)
        VALUES (0, 'UPMS에서의 첫걸음, 어떠셨나요?', 'PUBLISHED', false, false, NOW(), NOW())
        RETURNING id INTO v_form_id;

        INSERT INTO form_section (form_id, title, order_no, created_at, updated_at)
        VALUES (v_form_id, '사용자 조사', 1, NOW(), NOW())
        RETURNING id INTO v_section_id;

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '다음에도 이 방식으로 팀 매칭을 하고 싶나요?', 'RADIO', true, 1, true, NOW(), NOW())
        RETURNING id INTO v_question_id;

        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at)
        VALUES (v_question_id, '아니요', 1, false, NOW(), NOW()),
               (v_question_id, '좋아요', 2, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '매칭 결과를 확인하는 화면은 어떠셨나요?', 'RADIO', true, 2, true, NOW(), NOW())
        RETURNING id INTO v_question_id;

        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at)
        VALUES (v_question_id, '어려워요', 1, false, NOW(), NOW()),
               (v_question_id, '조금 어려워요', 2, false, NOW(), NOW()),
               (v_question_id, '보통이에요', 3, false, NOW(), NOW()),
               (v_question_id, '조금 이해돼요', 4, false, NOW(), NOW()),
               (v_question_id, '이해돼요', 5, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '매칭할 때 가장 유용했던 기능은 무엇인가요?', 'RADIO', true, 3, true, NOW(), NOW())
        RETURNING id INTO v_question_id;

        INSERT INTO question_option (question_id, content, order_no, is_other, created_at, updated_at)
        VALUES (v_question_id, '차수 별 매칭 결과 확인', 1, false, NOW(), NOW()),
               (v_question_id, '합격·불합격 결과 확인', 2, false, NOW(), NOW()),
               (v_question_id, '최종 팀원 구성 확인', 3, false, NOW(), NOW()),
               (v_question_id, '매칭 현황', 4, false, NOW(), NOW()),
               (v_question_id, '매칭 차수 설정', 5, false, NOW(), NOW());

        INSERT INTO question (form_section_id, title, type, is_required, order_no, is_active, created_at, updated_at)
        VALUES (v_section_id, '가장 좋았거나 아쉬웠던 점은 무엇인가요? (선택)', 'LONG_TEXT', false, 4, true, NOW(), NOW()),
               (v_section_id, '추가로 남기고 싶은 의견이 있나요? (선택)', 'LONG_TEXT', false, 5, true, NOW(), NOW());

        INSERT INTO user_feedback_template (context, target_type, form_id, is_active, created_at, updated_at)
        VALUES ('MATCHING_COMPLETED', 'CHALLENGER', v_form_id, true, NOW(), NOW());
    END
$$;
