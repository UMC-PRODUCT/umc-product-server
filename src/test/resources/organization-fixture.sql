-- Fixed IDs for organization-related test data.
-- schools: 1, 2, 3
-- gisus: 1, 2
-- chapters: 1, 2
-- chapter_schools: 1, 2, 3
-- members: 1, 2, 3
-- challengers: 1, 2, 3

INSERT INTO public.school (id, created_at, updated_at, logo_image_id, name, remark) VALUES (1, TIMESTAMPTZ '2026-02-28 15:00:00+00', TIMESTAMPTZ '2026-02-28 15:00:00+00', NULL, '가천대학교', NULL);
INSERT INTO public.school (id, created_at, updated_at, logo_image_id, name, remark) VALUES (2, TIMESTAMPTZ '2026-02-28 15:00:00+00', TIMESTAMPTZ '2026-02-28 15:00:00+00', NULL, '가톨릭대학교', NULL);
INSERT INTO public.school (id, created_at, updated_at, logo_image_id, name, remark) VALUES (3, TIMESTAMPTZ '2026-02-28 15:00:00+00', TIMESTAMPTZ '2026-02-28 15:00:00+00', NULL, '강릉원주대학교', NULL);

INSERT INTO public.gisu (id, is_active, created_at, end_at, generation, start_at, updated_at) VALUES (1, false, TIMESTAMPTZ '2026-02-27 20:17:46+00', TIMESTAMPTZ '2025-02-28 20:12:00+00', 7, TIMESTAMPTZ '2024-08-30 20:12:00+00', TIMESTAMPTZ '2026-02-27 20:17:46+00');
INSERT INTO public.gisu (id, is_active, created_at, end_at, generation, start_at, updated_at) VALUES (2, true, TIMESTAMPTZ '2026-02-27 20:16:26+00', TIMESTAMPTZ '2026-08-30 20:12:00+00', 10, TIMESTAMPTZ '2026-02-28 20:12:00+00', TIMESTAMPTZ '2026-02-27 20:16:26+00');

INSERT INTO public.chapter (id, created_at, gisu_id, updated_at, name) VALUES (1, TIMESTAMPTZ '2026-02-27 20:39:25+00', 1, TIMESTAMPTZ '2026-02-27 20:39:25+00', 'Ain');
INSERT INTO public.chapter (id, created_at, gisu_id, updated_at, name) VALUES (2, TIMESTAMPTZ '2026-02-27 20:39:25+00', 2, TIMESTAMPTZ '2026-02-27 20:39:25+00', 'Nova');

INSERT INTO public.chapter_school (id, chapter_id, created_at, school_id, updated_at) VALUES (1, 1, TIMESTAMPTZ '2026-02-27 20:39:25+00', 1, TIMESTAMPTZ '2026-02-27 20:39:25+00');
INSERT INTO public.chapter_school (id, chapter_id, created_at, school_id, updated_at) VALUES (2, 2, TIMESTAMPTZ '2026-02-27 20:39:25+00', 2, TIMESTAMPTZ '2026-02-27 20:39:25+00');
INSERT INTO public.chapter_school (id, chapter_id, created_at, school_id, updated_at) VALUES (3, 2, TIMESTAMPTZ '2026-02-27 20:39:25+00', 3, TIMESTAMPTZ '2026-02-27 20:39:25+00');

INSERT INTO public.member (id, created_at, profile_id, school_id, updated_at, nickname, status, name, email, profile_image_id) VALUES (1, TIMESTAMPTZ '2026-02-28 15:10:00+00', NULL, 1, TIMESTAMPTZ '2026-02-28 15:10:00+00', '가천멤버', 'ACTIVE', '가천멤버', 'member1@test.com', NULL);
INSERT INTO public.member (id, created_at, profile_id, school_id, updated_at, nickname, status, name, email, profile_image_id) VALUES (2, TIMESTAMPTZ '2026-02-28 15:10:00+00', NULL, 2, TIMESTAMPTZ '2026-02-28 15:10:00+00', '가톨릭멤버', 'ACTIVE', '가톨릭멤버', 'member2@test.com', NULL);
INSERT INTO public.member (id, created_at, profile_id, school_id, updated_at, nickname, status, name, email, profile_image_id) VALUES (3, TIMESTAMPTZ '2026-02-28 15:10:00+00', NULL, 3, TIMESTAMPTZ '2026-02-28 15:10:00+00', '강릉멤버', 'ACTIVE', '강릉멤버', 'member3@test.com', NULL);

INSERT INTO public.challenger (id, created_at, gisu_id, member_id, modified_by, updated_at, modification_reason, part, status) VALUES (1, TIMESTAMPTZ '2026-02-28 15:20:00+00', 1, 1, NULL, TIMESTAMPTZ '2026-02-28 15:20:00+00', NULL, 'WEB', 'ACTIVE');
INSERT INTO public.challenger (id, created_at, gisu_id, member_id, modified_by, updated_at, modification_reason, part, status) VALUES (2, TIMESTAMPTZ '2026-02-28 15:20:00+00', 2, 2, NULL, TIMESTAMPTZ '2026-02-28 15:20:00+00', NULL, 'SPRINGBOOT', 'ACTIVE');
INSERT INTO public.challenger (id, created_at, gisu_id, member_id, modified_by, updated_at, modification_reason, part, status) VALUES (3, TIMESTAMPTZ '2026-02-28 15:20:00+00', 2, 3, NULL, TIMESTAMPTZ '2026-02-28 15:20:00+00', NULL, 'SPRINGBOOT', 'ACTIVE');

SELECT setval('public.school_id_seq', 3, true);
SELECT setval('public.gisu_id_seq', 2, true);
SELECT setval('public.chapter_id_seq', 2, true);
SELECT setval('public.chapter_school_id_seq', 3, true);
SELECT setval('public.member_id_seq', 3, true);
SELECT setval('public.challenger_id_seq', 3, true);
