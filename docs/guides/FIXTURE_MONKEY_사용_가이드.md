# FixtureMonkey 사용 가이드

FixtureMonkey는 테스트용 객체를 빠르게 생성하고 필요한 필드만 조정하는 데 사용합니다. 프로젝트에서는 공용 MONKEY 인스턴스를 통해 일관된 생성 규칙을 유지합니다.

## 1. 뼈대 잡기 (시작 메서드)

- `giveMeOne(Class<T>)`
    - 조작 없이 즉시 객체 1개를 생성합니다.
    - 예: `Member member = MONKEY.giveMeOne(Member.class);`
- `giveMeBuilder(Class<T>)`
    - 필드 조작이 필요한 경우 `ArbitraryBuilder`를 반환합니다.
    - 예: `ArbitraryBuilder<Member> builder = MONKEY.giveMeBuilder(Member.class);`

## 2. 입맛대로 조작하기 (체이닝 메서드)

- `set(String property, Object value)`
    - 특정 필드를 원하는 값으로 고정합니다.
    - 내부 객체까지 깊게 지정 가능합니다.
    - 예: `.set("name", "고정된이름")`, `.set("school.name", "중앙대학교")`
- `setNull(String property)` / `setNotNull(String property)`
    - 필드의 null 여부를 강제합니다.
- `size(String property, int size)`
    - 컬렉션의 크기를 고정합니다.
- `minSize(String property, int min)` / `maxSize(String property, int max)`
    - 컬렉션 크기 범위를 제한합니다.
- `inner(ArbitraryBuilder<?> innerBuilder)`
    - 컬렉션 내부 요소나 복잡한 내부 객체에 다른 빌더를 주입합니다.

## 3. 결과물 뽑기 (종결 메서드)

- `sample()`
    - 조작된 빌더를 실행해 객체 1개를 반환합니다.
- `sampleList(int size)`
    - 동일 조건의 객체를 지정한 개수만큼 반환합니다.
- `sampleStream()`
    - 무한 스트림을 반환합니다. 대량 데이터 생성에 사용합니다.

## 실전 조합 예시

```java
StudyGroup group = FixtureCommon.MONKEY.giveMeBuilder(StudyGroup.class)
    .set("name", "스프링 백엔드 스터디")
    .setNotNull("gisuId")
    .size("members", 3)
    .set("members[*].status", "ACTIVE")
    .sample();
```

## 사용 팁

- 테스트는 결정적이어야 하므로, 중요한 필드는 `set`/`setNotNull`로 고정하는 것을 권장합니다.
- 컬렉션 크기와 상태 값은 `size`, `set`을 조합해 의도를 명확히 드러내세요.
- 복잡한 내부 구조는 `inner`로 분리해 재사용 가능한 빌더를 구성하세요.

