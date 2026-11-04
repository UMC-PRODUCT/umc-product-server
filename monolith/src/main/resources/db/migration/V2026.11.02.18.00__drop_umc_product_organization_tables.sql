-- ADR-006 UMC PRODUCT 조직 모델 폐기. 코드와 API는 같은 변경에서 제거했다.
-- btree_gist 확장은 남긴다. 확장 삭제는 DB 권한에 따라 실패할 수 있고, 남겨도 동작에 영향이 없다.
DROP TABLE IF EXISTS umc_product_squad_participant;
DROP TABLE IF EXISTS umc_product_chapter_membership;
DROP TABLE IF EXISTS umc_product_leadership;
DROP TABLE IF EXISTS umc_product_member_activity_period;
DROP TABLE IF EXISTS umc_product_chapter;
DROP TABLE IF EXISTS umc_product_squad;
DROP TABLE IF EXISTS umc_product_member;
