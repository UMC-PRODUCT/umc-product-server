-- 약관
INSERT INTO public.term (active, required, created_at, id, updated_at, link, type)
VALUES (true, true, '2026-02-26 09:03:00.786000 +00:00', 1, '2026-02-26 09:03:18.503000 +00:00',
        'https://makeus-challenge.notion.site/300b57f4596b803f8c94dd4f4fb71960?source=copy_link', 'PRIVACY');
INSERT INTO public.term (active, required, created_at, id, updated_at, link, type)
VALUES (true, true, '2026-02-26 09:03:00.786000 +00:00', 2, '2026-02-26 09:03:18.503000 +00:00',
        'https://makeus-challenge.notion.site/300b57f4596b803f8c94dd4f4fb71960?source=copy_link', 'SERVICE');

-- 기수
INSERT INTO public.gisu (is_active, created_at, end_at, generation, id, start_at, updated_at)
VALUES (false, '2026-02-27 20:17:01.307592 +00:00', '2024-08-30 20:12:00.000000 +00:00', 6, 1,
        '2024-02-29 20:12:00.000000 +00:00', '2026-02-27 20:17:01.307592 +00:00');
INSERT INTO public.gisu (is_active, created_at, end_at, generation, id, start_at, updated_at)
VALUES (false, '2026-02-27 20:17:46.723468 +00:00', '2025-02-28 20:12:00.000000 +00:00', 7, 2,
        '2024-08-30 20:12:00.000000 +00:00', '2026-02-27 20:17:46.723468 +00:00');
INSERT INTO public.gisu (is_active, created_at, end_at, generation, id, start_at, updated_at)
VALUES (false, '2026-02-27 20:16:41.951075 +00:00', '2025-08-30 20:12:00.000000 +00:00', 8, 3,
        '2025-02-28 20:12:00.000000 +00:00', '2026-02-27 20:16:41.951075 +00:00');
INSERT INTO public.gisu (is_active, created_at, end_at, generation, id, start_at, updated_at)
VALUES (false, '2026-02-27 20:18:07.932695 +00:00', '2026-02-28 20:12:00.000000 +00:00', 9, 4,
        '2025-08-30 20:12:00.000000 +00:00', '2026-02-27 20:18:07.932695 +00:00');
INSERT INTO public.gisu (is_active, created_at, end_at, generation, id, start_at, updated_at)
VALUES (true, '2026-02-27 20:16:26.117792 +00:00', '2026-08-30 20:12:00.000000 +00:00', 10, 5,
        '2026-02-28 20:12:00.000000 +00:00', '2026-02-27 20:16:26.117792 +00:00');

-- 학교
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 1, '2026-02-28 15:00:00.000000 +00:00', null, '가천대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 2, '2026-02-28 15:00:00.000000 +00:00', null, '가톨릭대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 3, '2026-02-28 15:00:00.000000 +00:00', null, '강릉원주대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 4, '2026-02-28 15:00:00.000000 +00:00', null, '경상국립대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 5, '2026-02-28 15:00:00.000000 +00:00', null, '경희대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 6, '2026-02-28 15:00:00.000000 +00:00', null, '광운대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 7, '2026-02-28 15:00:00.000000 +00:00', null, '국립부경대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 8, '2026-02-28 15:00:00.000000 +00:00', null, '단국대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 9, '2026-02-28 15:00:00.000000 +00:00', null, '덕성여자대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 10, '2026-02-28 15:00:00.000000 +00:00', null, '동국대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 11, '2026-02-28 15:00:00.000000 +00:00', null, '동덕여자대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 12, '2026-02-28 15:00:00.000000 +00:00', null, '동아대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 13, '2026-02-28 15:00:00.000000 +00:00', null, '동양미래대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 14, '2026-02-28 15:00:00.000000 +00:00', null, '명지대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 15, '2026-02-28 15:00:00.000000 +00:00', null, '상명대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 16, '2026-02-28 15:00:00.000000 +00:00', null, '서경대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 17, '2026-02-28 15:00:00.000000 +00:00', null, '서울여자대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 18, '2026-02-28 15:00:00.000000 +00:00', null, '성신여자대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 19, '2026-02-28 15:00:00.000000 +00:00', null, '숙명여자대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 20, '2026-02-28 15:00:00.000000 +00:00', null, '숭실대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 21, '2026-02-28 15:00:00.000000 +00:00', null, '안양대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 22, '2026-02-28 15:00:00.000000 +00:00', null, '연세대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 23, '2026-02-28 15:00:00.000000 +00:00', null, '영남대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 24, '2026-02-28 15:00:00.000000 +00:00', null, '울산대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 25, '2026-02-28 15:00:00.000000 +00:00', null, '이화여자대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 26, '2026-02-28 15:00:00.000000 +00:00', null, '인제대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 27, '2026-02-28 15:00:00.000000 +00:00', null, '인하대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 28, '2026-02-28 15:00:00.000000 +00:00', null, '전북대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 29, '2026-02-28 15:00:00.000000 +00:00', null, '중앙대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 30, '2026-02-28 15:00:00.000000 +00:00', null, '한국공학대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 31, '2026-02-28 15:00:00.000000 +00:00', null, '한국외국어대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 32, '2026-02-28 15:00:00.000000 +00:00', null, '한국항공대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 33, '2026-02-28 15:00:00.000000 +00:00', null, '한성대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 34, '2026-02-28 15:00:00.000000 +00:00', null, '한양대학교 ERICA', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 35, '2026-02-28 15:00:00.000000 +00:00', null, '한양사이버대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 36, '2026-02-28 15:00:00.000000 +00:00', null, '홍익대학교', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 37, '2026-02-28 15:00:00.000000 +00:00', null, '홍익대학교 서울캠퍼스', null);
INSERT INTO public.school (created_at, id, updated_at, logo_image_id, name, remark)
VALUES ('2026-02-28 15:00:00.000000 +00:00', 38, '2026-02-28 15:00:00.000000 +00:00', null, '홍익대학교 세종캠퍼스', null);

-- 지부
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:24.400185 +00:00', 1, 1, '2026-02-27 20:39:24.400185 +00:00', 'GOAT');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:24.847071 +00:00', 1, 2, '2026-02-27 20:39:24.847071 +00:00', '오션');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:24.879679 +00:00', 1, 3, '2026-02-27 20:39:24.879679 +00:00', '시리우스');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:24.915081 +00:00', 1, 4, '2026-02-27 20:39:24.915081 +00:00', '타이거');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:24.946091 +00:00', 1, 5, '2026-02-27 20:39:24.946091 +00:00', '트리');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:24.982004 +00:00', 1, 6, '2026-02-27 20:39:24.982004 +00:00', 'HESCK');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.017910 +00:00', 2, 7, '2026-02-27 20:39:25.017910 +00:00', 'Pegasus');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.045035 +00:00', 2, 8, '2026-02-27 20:39:25.045035 +00:00', 'Gemini');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.074532 +00:00', 2, 9, '2026-02-27 20:39:25.074532 +00:00', 'Sculptor');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.109816 +00:00', 2, 10, '2026-02-27 20:39:25.109816 +00:00', 'Pyxis');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.156232 +00:00', 2, 11, '2026-02-27 20:39:25.156232 +00:00', 'Cygnus');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.190264 +00:00', 2, 12, '2026-02-27 20:39:25.190264 +00:00', 'Orion');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.219278 +00:00', 2, 13, '2026-02-27 20:39:25.219278 +00:00', 'Draco');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.243300 +00:00', 3, 14, '2026-02-27 20:39:25.243300 +00:00', 'Ain');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.273329 +00:00', 3, 15, '2026-02-27 20:39:25.273329 +00:00', 'Sirius');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.297351 +00:00', 3, 16, '2026-02-27 20:39:25.297351 +00:00', 'Vega');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.326378 +00:00', 3, 17, '2026-02-27 20:39:25.326378 +00:00', 'Betelgeuse');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.354403 +00:00', 3, 18, '2026-02-27 20:39:25.354403 +00:00', 'Deneb');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.379887 +00:00', 3, 19, '2026-02-27 20:39:25.379887 +00:00', 'Maru');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.400037 +00:00', 4, 20, '2026-02-27 20:39:25.400037 +00:00', 'Nova');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.424601 +00:00', 4, 21, '2026-02-27 20:39:25.424601 +00:00', 'Aquarius');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.451634 +00:00', 4, 22, '2026-02-27 20:39:25.451634 +00:00', 'Cetus');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.477658 +00:00', 4, 23, '2026-02-27 20:39:25.477658 +00:00', 'Pegasus');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.504093 +00:00', 4, 24, '2026-02-27 20:39:25.504093 +00:00', 'Leo');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.532587 +00:00', 4, 25, '2026-02-27 20:39:25.532587 +00:00', 'Scorpio');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.563044 +00:00', 4, 26, '2026-02-27 20:39:25.563044 +00:00', 'Cassiopeia');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.593587 +00:00', 5, 27, '2026-02-27 20:39:25.593587 +00:00', 'Neon');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.625452 +00:00', 5, 28, '2026-02-27 20:39:25.625452 +00:00', 'Xenon');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.655479 +00:00', 5, 29, '2026-02-27 20:39:25.655479 +00:00', 'Chromium');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.681954 +00:00', 5, 30, '2026-02-27 20:39:25.681954 +00:00', 'Ferrum');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.718657 +00:00', 5, 31, '2026-02-27 20:39:25.718657 +00:00', 'Platinum');
INSERT INTO public.chapter (created_at, gisu_id, id, updated_at, name)
VALUES ('2026-02-27 20:39:25.742045 +00:00', 5, 32, '2026-02-27 20:39:25.742045 +00:00', 'Selenium');

-- 지부-학교 매핑
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (1, '2026-02-27 20:39:24.484701 +00:00', 1, 1, '2026-02-27 20:39:24.484701 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (1, '2026-02-27 20:39:24.490707 +00:00', 2, 5, '2026-02-27 20:39:24.490707 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (1, '2026-02-27 20:39:24.493709 +00:00', 3, 14, '2026-02-27 20:39:24.493709 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (1, '2026-02-27 20:39:24.814042 +00:00', 4, 31, '2026-02-27 20:39:24.814042 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (1, '2026-02-27 20:39:24.819046 +00:00', 5, 34, '2026-02-27 20:39:24.819046 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (2, '2026-02-27 20:39:24.858517 +00:00', 6, 2, '2026-02-27 20:39:24.858517 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (2, '2026-02-27 20:39:24.861658 +00:00', 7, 20, '2026-02-27 20:39:24.861658 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (2, '2026-02-27 20:39:24.863664 +00:00', 8, 27, '2026-02-27 20:39:24.863664 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (2, '2026-02-27 20:39:24.866666 +00:00', 9, 30, '2026-02-27 20:39:24.866666 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (3, '2026-02-27 20:39:24.901699 +00:00', 10, 4, '2026-02-27 20:39:24.901699 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (3, '2026-02-27 20:39:24.903670 +00:00', 11, 24, '2026-02-27 20:39:24.903670 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (4, '2026-02-27 20:39:24.925832 +00:00', 12, 6, '2026-02-27 20:39:24.925832 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (4, '2026-02-27 20:39:24.927834 +00:00', 13, 16, '2026-02-27 20:39:24.927834 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (4, '2026-02-27 20:39:24.929836 +00:00', 14, 17, '2026-02-27 20:39:24.929836 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (4, '2026-02-27 20:39:24.932331 +00:00', 15, 18, '2026-02-27 20:39:24.932331 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (4, '2026-02-27 20:39:24.934686 +00:00', 16, 33, '2026-02-27 20:39:24.934686 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (5, '2026-02-27 20:39:24.958649 +00:00', 17, 9, '2026-02-27 20:39:24.958649 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (5, '2026-02-27 20:39:24.960651 +00:00', 18, 10, '2026-02-27 20:39:24.960651 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (5, '2026-02-27 20:39:24.963654 +00:00', 19, 11, '2026-02-27 20:39:24.963654 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (5, '2026-02-27 20:39:24.966657 +00:00', 20, 15, '2026-02-27 20:39:24.966657 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (5, '2026-02-27 20:39:24.968658 +00:00', 21, 22, '2026-02-27 20:39:24.968658 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (6, '2026-02-27 20:39:24.996206 +00:00', 22, 19, '2026-02-27 20:39:24.996206 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (6, '2026-02-27 20:39:24.998400 +00:00', 23, 25, '2026-02-27 20:39:24.998400 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (6, '2026-02-27 20:39:25.001404 +00:00', 24, 29, '2026-02-27 20:39:25.001404 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (6, '2026-02-27 20:39:25.003406 +00:00', 25, 32, '2026-02-27 20:39:25.003406 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (6, '2026-02-27 20:39:25.004897 +00:00', 26, 36, '2026-02-27 20:39:25.004897 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (7, '2026-02-27 20:39:25.027918 +00:00', 27, 1, '2026-02-27 20:39:25.027918 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (7, '2026-02-27 20:39:25.029921 +00:00', 28, 5, '2026-02-27 20:39:25.029921 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (7, '2026-02-27 20:39:25.032923 +00:00', 29, 14, '2026-02-27 20:39:25.032923 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (7, '2026-02-27 20:39:25.034925 +00:00', 30, 31, '2026-02-27 20:39:25.034925 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (8, '2026-02-27 20:39:25.056514 +00:00', 31, 2, '2026-02-27 20:39:25.056514 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (8, '2026-02-27 20:39:25.059517 +00:00', 32, 27, '2026-02-27 20:39:25.059517 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (8, '2026-02-27 20:39:25.061526 +00:00', 33, 30, '2026-02-27 20:39:25.061526 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (8, '2026-02-27 20:39:25.063523 +00:00', 34, 34, '2026-02-27 20:39:25.063523 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (9, '2026-02-27 20:39:25.085798 +00:00', 35, 6, '2026-02-27 20:39:25.085798 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (9, '2026-02-27 20:39:25.088800 +00:00', 36, 9, '2026-02-27 20:39:25.088800 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (9, '2026-02-27 20:39:25.090801 +00:00', 37, 10, '2026-02-27 20:39:25.090801 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (9, '2026-02-27 20:39:25.093804 +00:00', 38, 15, '2026-02-27 20:39:25.093804 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (10, '2026-02-27 20:39:25.124829 +00:00', 39, 11, '2026-02-27 20:39:25.124829 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (10, '2026-02-27 20:39:25.128833 +00:00', 40, 16, '2026-02-27 20:39:25.128833 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (10, '2026-02-27 20:39:25.131835 +00:00', 41, 32, '2026-02-27 20:39:25.131835 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (10, '2026-02-27 20:39:25.135839 +00:00', 42, 33, '2026-02-27 20:39:25.135839 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (11, '2026-02-27 20:39:25.169244 +00:00', 43, 17, '2026-02-27 20:39:25.169244 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (11, '2026-02-27 20:39:25.172249 +00:00', 44, 18, '2026-02-27 20:39:25.172249 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (11, '2026-02-27 20:39:25.175251 +00:00', 45, 20, '2026-02-27 20:39:25.175251 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (11, '2026-02-27 20:39:25.178253 +00:00', 46, 29, '2026-02-27 20:39:25.178253 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (12, '2026-02-27 20:39:25.200273 +00:00', 47, 19, '2026-02-27 20:39:25.200273 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (12, '2026-02-27 20:39:25.203276 +00:00', 48, 22, '2026-02-27 20:39:25.203276 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (12, '2026-02-27 20:39:25.206279 +00:00', 49, 25, '2026-02-27 20:39:25.206279 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (12, '2026-02-27 20:39:25.209282 +00:00', 50, 36, '2026-02-27 20:39:25.209282 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (13, '2026-02-27 20:39:25.231288 +00:00', 51, 24, '2026-02-27 20:39:25.231288 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (14, '2026-02-27 20:39:25.253308 +00:00', 52, 1, '2026-02-27 20:39:25.253308 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (14, '2026-02-27 20:39:25.256311 +00:00', 53, 14, '2026-02-27 20:39:25.256311 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (14, '2026-02-27 20:39:25.258313 +00:00', 54, 20, '2026-02-27 20:39:25.258313 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (14, '2026-02-27 20:39:25.261316 +00:00', 55, 29, '2026-02-27 20:39:25.261316 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (14, '2026-02-27 20:39:25.263325 +00:00', 56, 34, '2026-02-27 20:39:25.263325 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (15, '2026-02-27 20:39:25.283339 +00:00', 57, 2, '2026-02-27 20:39:25.283339 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (15, '2026-02-27 20:39:25.285340 +00:00', 58, 25, '2026-02-27 20:39:25.285340 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (15, '2026-02-27 20:39:25.287341 +00:00', 59, 32, '2026-02-27 20:39:25.287341 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (16, '2026-02-27 20:39:25.307360 +00:00', 60, 6, '2026-02-27 20:39:25.307360 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (16, '2026-02-27 20:39:25.309362 +00:00', 61, 16, '2026-02-27 20:39:25.309362 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (16, '2026-02-27 20:39:25.311363 +00:00', 62, 17, '2026-02-27 20:39:25.311363 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (16, '2026-02-27 20:39:25.314366 +00:00', 63, 18, '2026-02-27 20:39:25.314366 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (16, '2026-02-27 20:39:25.316368 +00:00', 64, 33, '2026-02-27 20:39:25.316368 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (17, '2026-02-27 20:39:25.336386 +00:00', 65, 9, '2026-02-27 20:39:25.336386 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (17, '2026-02-27 20:39:25.339388 +00:00', 66, 10, '2026-02-27 20:39:25.339388 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (17, '2026-02-27 20:39:25.342392 +00:00', 67, 11, '2026-02-27 20:39:25.342392 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (17, '2026-02-27 20:39:25.344394 +00:00', 68, 31, '2026-02-27 20:39:25.344394 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (18, '2026-02-27 20:39:25.363873 +00:00', 69, 15, '2026-02-27 20:39:25.363873 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (18, '2026-02-27 20:39:25.366281 +00:00', 70, 19, '2026-02-27 20:39:25.366281 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (18, '2026-02-27 20:39:25.368878 +00:00', 71, 27, '2026-02-27 20:39:25.368878 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (18, '2026-02-27 20:39:25.370879 +00:00', 72, 36, '2026-02-27 20:39:25.370879 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (19, '2026-02-27 20:39:25.390897 +00:00', 73, 28, '2026-02-27 20:39:25.390897 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (20, '2026-02-27 20:39:25.408509 +00:00', 74, 1, '2026-02-27 20:39:25.408509 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (20, '2026-02-27 20:39:25.410038 +00:00', 75, 3, '2026-02-27 20:39:25.410038 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (20, '2026-02-27 20:39:25.412589 +00:00', 76, 20, '2026-02-27 20:39:25.412589 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (20, '2026-02-27 20:39:25.415592 +00:00', 77, 35, '2026-02-27 20:39:25.415592 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (21, '2026-02-27 20:39:25.433112 +00:00', 78, 2, '2026-02-27 20:39:25.433112 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (21, '2026-02-27 20:39:25.435620 +00:00', 79, 32, '2026-02-27 20:39:25.435620 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (21, '2026-02-27 20:39:25.438623 +00:00', 80, 37, '2026-02-27 20:39:25.438623 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (21, '2026-02-27 20:39:25.440624 +00:00', 81, 38, '2026-02-27 20:39:25.440624 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (22, '2026-02-27 20:39:25.460642 +00:00', 82, 6, '2026-02-27 20:39:25.460642 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (22, '2026-02-27 20:39:25.463645 +00:00', 83, 9, '2026-02-27 20:39:25.463645 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (22, '2026-02-27 20:39:25.466648 +00:00', 84, 16, '2026-02-27 20:39:25.466648 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (22, '2026-02-27 20:39:25.468650 +00:00', 85, 18, '2026-02-27 20:39:25.468650 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (23, '2026-02-27 20:39:25.485661 +00:00', 86, 7, '2026-02-27 20:39:25.485661 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (23, '2026-02-27 20:39:25.487574 +00:00', 87, 12, '2026-02-27 20:39:25.487574 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (23, '2026-02-27 20:39:25.490578 +00:00', 88, 23, '2026-02-27 20:39:25.490578 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (23, '2026-02-27 20:39:25.492581 +00:00', 89, 24, '2026-02-27 20:39:25.492581 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (23, '2026-02-27 20:39:25.494582 +00:00', 90, 26, '2026-02-27 20:39:25.494582 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (24, '2026-02-27 20:39:25.513104 +00:00', 91, 8, '2026-02-27 20:39:25.513104 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (24, '2026-02-27 20:39:25.516510 +00:00', 92, 11, '2026-02-27 20:39:25.516510 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (24, '2026-02-27 20:39:25.518511 +00:00', 93, 25, '2026-02-27 20:39:25.518511 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (24, '2026-02-27 20:39:25.520512 +00:00', 94, 29, '2026-02-27 20:39:25.520512 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (24, '2026-02-27 20:39:25.523580 +00:00', 95, 31, '2026-02-27 20:39:25.523580 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (25, '2026-02-27 20:39:25.542178 +00:00', 96, 10, '2026-02-27 20:39:25.542178 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (25, '2026-02-27 20:39:25.545181 +00:00', 97, 14, '2026-02-27 20:39:25.545181 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (25, '2026-02-27 20:39:25.547185 +00:00', 98, 15, '2026-02-27 20:39:25.547185 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (25, '2026-02-27 20:39:25.550189 +00:00', 99, 17, '2026-02-27 20:39:25.550189 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (25, '2026-02-27 20:39:25.552192 +00:00', 100, 30, '2026-02-27 20:39:25.552192 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (26, '2026-02-27 20:39:25.573522 +00:00', 101, 13, '2026-02-27 20:39:25.573522 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (26, '2026-02-27 20:39:25.577526 +00:00', 102, 19, '2026-02-27 20:39:25.577526 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (26, '2026-02-27 20:39:25.580040 +00:00', 103, 27, '2026-02-27 20:39:25.580040 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (26, '2026-02-27 20:39:25.581927 +00:00', 104, 33, '2026-02-27 20:39:25.581927 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (26, '2026-02-27 20:39:25.583929 +00:00', 105, 34, '2026-02-27 20:39:25.583929 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (27, '2026-02-27 20:39:25.603099 +00:00', 106, 1, '2026-02-27 20:39:25.603099 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (27, '2026-02-27 20:39:25.609587 +00:00', 107, 11, '2026-02-27 20:39:25.609587 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (27, '2026-02-27 20:39:25.611879 +00:00', 108, 19, '2026-02-27 20:39:25.611879 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (27, '2026-02-27 20:39:25.614267 +00:00', 109, 27, '2026-02-27 20:39:25.614267 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (27, '2026-02-27 20:39:25.616443 +00:00', 110, 32, '2026-02-27 20:39:25.616443 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (28, '2026-02-27 20:39:25.634459 +00:00', 111, 2, '2026-02-27 20:39:25.634459 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (28, '2026-02-27 20:39:25.637463 +00:00', 112, 8, '2026-02-27 20:39:25.637463 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (28, '2026-02-27 20:39:25.640465 +00:00', 113, 9, '2026-02-27 20:39:25.640465 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (28, '2026-02-27 20:39:25.642467 +00:00', 114, 29, '2026-02-27 20:39:25.642467 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (28, '2026-02-27 20:39:25.644469 +00:00', 115, 33, '2026-02-27 20:39:25.644469 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (29, '2026-02-27 20:39:25.663486 +00:00', 116, 6, '2026-02-27 20:39:25.663486 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (29, '2026-02-27 20:39:25.665488 +00:00', 117, 13, '2026-02-27 20:39:25.665488 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (29, '2026-02-27 20:39:25.667489 +00:00', 118, 17, '2026-02-27 20:39:25.667489 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (29, '2026-02-27 20:39:25.669491 +00:00', 119, 30, '2026-02-27 20:39:25.669491 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (29, '2026-02-27 20:39:25.671493 +00:00', 120, 31, '2026-02-27 20:39:25.671493 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (30, '2026-02-27 20:39:25.689630 +00:00', 121, 10, '2026-02-27 20:39:25.689630 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (30, '2026-02-27 20:39:25.704642 +00:00', 122, 25, '2026-02-27 20:39:25.704642 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (30, '2026-02-27 20:39:25.706645 +00:00', 123, 37, '2026-02-27 20:39:25.706645 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (30, '2026-02-27 20:39:25.708647 +00:00', 124, 38, '2026-02-27 20:39:25.708647 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (31, '2026-02-27 20:39:25.727644 +00:00', 125, 12, '2026-02-27 20:39:25.727644 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (31, '2026-02-27 20:39:25.730647 +00:00', 126, 23, '2026-02-27 20:39:25.730647 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (31, '2026-02-27 20:39:25.732649 +00:00', 127, 26, '2026-02-27 20:39:25.732649 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (32, '2026-02-27 20:39:25.749052 +00:00', 128, 16, '2026-02-27 20:39:25.749052 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (32, '2026-02-27 20:39:25.751053 +00:00', 129, 18, '2026-02-27 20:39:25.751053 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (32, '2026-02-27 20:39:25.754056 +00:00', 130, 20, '2026-02-27 20:39:25.754056 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (32, '2026-02-27 20:39:25.756058 +00:00', 131, 21, '2026-02-27 20:39:25.756058 +00:00');
INSERT INTO public.chapter_school (chapter_id, created_at, id, school_id, updated_at)
VALUES (32, '2026-02-27 20:39:25.757503 +00:00', 132, 34, '2026-02-27 20:39:25.757503 +00:00');
