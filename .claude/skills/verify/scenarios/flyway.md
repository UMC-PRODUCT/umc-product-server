# flyway: 빈 DB 적용과 재기동

`monolith/src/main/resources/db/migration`에 파일을 추가하거나 고쳤을 때 쓴다.

## 확인하는 것

- 빈 DB에서 모든 마이그레이션이 순서대로 성공한다.
- 같은 DB로 다시 기동했을 때 새로 적용되는 마이그레이션이 없다.
- 새 마이그레이션이 만든 테이블·컬럼·제약이 실제로 있다.

## 실행

```bash
V=.claude/skills/verify/scripts/verify.sh

# up 이 이미 빈 DB 적용을 확인한다 (evidence/flyway.txt)
$V flyway

# 같은 DB로 앱만 다시 띄우고 다시 센다
$V restart-app
$V flyway
```

새 마이그레이션이 만든 구조는 직접 조회해서 증거로 남긴다. 컨테이너 이름은 `doctor` 출력에 있다.

```bash
docker exec <DB 컨테이너> psql -U verify -d umc_product -c '\d <테이블>' > .verify/current/evidence/schema-<테이블>.txt
```

## 판정

- 두 번의 `flyway` 출력에서 `migration_files`와 `applied_success`가 같다.
- `evidence/app-before-restart.log`가 아닌 재기동 후 `app.log`에 새 마이그레이션 적용 로그가 없다.

## 주의

- 파일 이름 규칙은 `VYYYY.MM.DD.HH.MM__snake_case.sql`이고 버전 중복은 Gradle이 검사한다. 이 시나리오는 실제 PostGIS에서 SQL이 도는지를 본다.
- 이미 배포된 마이그레이션 파일을 고치면 운영 DB의 체크섬과 어긋난다. 이 시나리오는 빈 DB에서 시작하므로 그 문제를 잡지 못한다.
