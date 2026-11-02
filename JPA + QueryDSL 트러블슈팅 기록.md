# JPA + QueryDSL 트러블슈팅 기록

Spring Boot 2.7.17 / Java 11 / Gradle 8.14 / MariaDB / Eclipse(STS) 환경에서
`baseballJpaDsl`, `univJpaDsl` 두 프로젝트를 만들며 겪은 에러와 해결 방법을 정리했습니다.

## 목차

- [환경](#환경)
- [A. baseballJpaDsl](#a-baseballjpadsl)
- [B. univJpaDsl](#b-univjpadsl)
- [최종 build.gradle (QueryDSL 설정)](#최종-buildgradle-querydsl-설정)
- [배운 점](#배운-점)

---

## 환경

| 항목 | 버전 |
|---|---|
| Spring Boot | 2.7.17 (`javax.persistence` 사용) |
| Java | 11 |
| Gradle | 8.14.4 |
| DB | MariaDB |
| QueryDSL | 5.0.0 |
| IDE | Eclipse / STS |

> Spring Boot 3.x는 `jakarta.persistence`, 2.x는 `javax.persistence`입니다.

---

## A. baseballJpaDsl

### 1. QueryDSL / ModelMapper import 안 됨

- **증상**: `JPAQueryFactory`, `ModelMapper`를 찾을 수 없음
- **원인**: build.gradle에 의존성이 없었음
- **해결**: 의존성 추가 (아래 최종 build.gradle 참고)

### 2. 엔티티/DTO 컴파일 에러

- **원인**
  - `PlayerDto.toEntity()`에서 `.build()` 위치가 꼬이고 `PlayerEntity.setTeam`처럼 클래스명으로 호출
  - 필드는 `teamname`인데 `setTeamName` 호출 (대소문자 불일치)
  - `TeamEntity`가 다른 프로젝트 패키지(`com.kosta.baseball...`)를 import
  - `TeamDto` 파일 없음
- **해결**: 변수(`playerEntity.setTeam(...)`)로 호출, 필드명 통일, import 정리, `TeamDto` 작성

### 3. `compileJava FAILED` → Q클래스 생성 안 됨

```
error: cannot find symbol
  symbol:   class JpaQueryFactory
```

- **원인**: 클래스명 오타. 정답은 `JPAQueryFactory` (JPA 전부 대문자)
- **핵심**: 컴파일이 실패하면 annotation processor가 끝까지 돌지 못해 Q클래스도 생성되지 않음

### 4. Eclipse에서 Q클래스가 안 보임 / Refresh하면 사라짐

- **원인**: 기본 생성 위치(`build/generated/sources/annotationProcessor/java/main`)를 Eclipse가 소스 폴더로 인식하지 못함
- **해결**: Q클래스 생성 위치를 `build/generated/querydsl`로 지정하고 소스 폴더로 등록 (아래 build.gradle 참고)
- **주의**: 수동으로 추가한 소스 폴더가 있으면 Remove (중복 클래스 방지)

### 5. `contextLoads()` 테스트 실패

```
UnsatisfiedDependencyException
  Caused by: NoSuchBeanDefinitionException
```

- **원인**: 주입하려는 빈을 찾지 못함 (`@Repository`/`@Service` 누락, 패키지 스캔 범위 등)
- **확인 방법**: `build/reports/tests/test/index.html`에서 `No qualifying bean of type '...'` 확인
- **임시 우회**: `gradlew build -x test`

### 6. Repository import 오류

| 잘못된 import | 올바른 import |
|---|---|
| `org.apache.ibatis.ognl.BooleanExpression` | `com.querydsl.core.types.dsl.BooleanExpression` |
| `org.hibernate.criterion.Projections` | `com.querydsl.core.types.Projections` |

### 7. 테이블 이름에 `_entity`가 붙음

- **원인**: `@Table`을 지정하지 않으면 클래스명(`PlayerEntity`)이 `player_entity`로 변환되고, `ddl-auto=update`가 새 테이블을 자동 생성
- **해결**: 기존 테이블을 쓰려면 `@Table(name = "player")`처럼 지정
- **주의**: DB에서만 이름을 바꾸면 Hibernate가 다시 `*_entity`를 만들 수 있으므로 코드와 DB를 함께 맞출 것

---

## B. univJpaDsl

### 1. `StudentEntity` 연관관계 오류

- **원인**: `@JoinColumn`만 있고 `@ManyToOne`이 없음. 또 `deptno1`(Integer)과 `@JoinColumn(name="deptno1")`이 같은 컬럼을 중복 매핑
- **해결**: `@ManyToOne` + `@JoinColumn`으로 `DepartmentEntity dept1`, `dept2`, `ProfessorEntity professor` 객체 관계로 변경, 기존 `Integer deptno1` 필드 삭제

```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "deptno1")
private DepartmentEntity dept1;
```

### 2. `setProfname` 에러

- **원인**: DTO 필드는 `profname`인데 `setProfName`(대문자 N)을 호출
- **해결**: Lombok setter는 필드명 기준이므로 철자를 DTO 필드명에 맞춤

### 3. `StudentDto.toEntity()` 에러

- **원인**: 엔티티 필드가 `deptno1` → `dept1`로 바뀌었는데 `setDeptno1`을 그대로 호출
- **해결**: `setDept1(...)`, `setDept2(...)`, `setProfessor(...)`

### 4. `builder()`가 전부 에러

- **원인 1**: `DepartmentEntity`에 `@Builder`와 `import lombok.Builder` 누락
- **원인 2**: 필드명 `build`가 Lombok의 `build()` 메서드와 혼동됨
- **해결**: `@Builder` 추가, 자바 필드명을 `building`으로 바꾸고 DB 컬럼명은 유지

```java
@Column(name = "build")
private String building;
```

### 5. `UnivServiceImpl` 문법 에러 다수

```
Syntax error, insert ";" to complete BlockStatements
Syntax error, insert "}" to complete ClassBody
findS cannot be resolved to a variable or is not a field
```

- **원인**: 쓰다 만 코드(`return studentRepository.findS`, `professorRepository.save(pr)`) 하나가 문법을 깨뜨려 뒤의 모든 메서드까지 연쇄 에러
- **팁**: Problems 탭에서 **맨 위 에러부터** 고칠 것. 나머지는 대부분 연쇄 에러

### 6. `saveDepartment` return type 에러

```
The return type is incompatible with UnivService.saveDepartment(DepartmentDto)
```

- **원인**: 인터페이스는 `Integer`, 구현은 `void` + 학과 저장에 `studentRepository` 사용
- **해결**: 인터페이스와 구현의 반환 타입 통일, `departMentRepository` 사용

### 7. `UnivRepository` 쿼리 오류

- `Colloge` 오타 → `College`
- `dept1.build` → `dept1.building` (필드명 변경 반영, Q클래스 재생성 필요)
- 대학 조회 where가 학과(`dept1`)를 비교 → 두 단계 위 `dept3`를 비교해야 함

```java
QDepartmentEntity dept1 = QDepartmentEntity.departmentEntity;
QDepartmentEntity dept2 = new QDepartmentEntity("dept2");
QDepartmentEntity dept3 = new QDepartmentEntity("dept3");

jpaQueryFactory.select(Projections.fields(DepartmentDto.class,
        dept1.deptno, dept1.dname, dept1.part, dept1.building))
    .from(dept1)
    .join(dept2).on(dept1.part.eq(dept2.deptno))   // 학과 -> 학부
    .join(dept3).on(dept2.part.eq(dept3.deptno))   // 학부 -> 대학
    .where(dept3.dname.eq(colName))
    .fetch();
```

> 같은 테이블을 여러 번 조인할 때는 `new QDepartmentEntity("별칭")`으로 별칭을 각각 만들어야 합니다.

### 8. 컨트롤러 에러

- **원인**: `/deptInfoByCollegeNo`가 `departmentListByBuild(colNo)`(건물명 String용)를 호출
- **해결**: `departmentListByCollegeNo(colNo)`로 변경. 컨트롤러가 부르는 서비스 메서드 이름이 `UnivService`/`UnivServiceImpl`에 똑같이 있어야 함

### 9. `String.java` 에러

- **원인**: JDK 내부 소스(`java.lang.String`)를 연 것으로, 프로젝트 에러가 아님
- **주의**: 프로젝트에 `java.lang` 패키지를 복사해 넣으면 안 됨

---

## 최종 build.gradle (QueryDSL 설정)

```gradle
plugins {
	id 'java'
	id 'war'
	id 'org.springframework.boot' version '2.7.17'
	id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

group = 'com.kosta'
version = '0.0.1-SNAPSHOT'

java {
	sourceCompatibility = '11'
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.3.1'

	// QueryDSL (버전 고정)
	implementation 'com.querydsl:querydsl-jpa:5.0.0'
	annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jpa'
	annotationProcessor 'jakarta.persistence:jakarta.persistence-api:2.2.3'
	annotationProcessor 'jakarta.annotation:jakarta.annotation-api:1.3.5'

	// ModelMapper
	implementation 'org.modelmapper:modelmapper:3.1.1'

	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'
	runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'

	providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
	testCompileOnly 'org.projectlombok:lombok'
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
	testAnnotationProcessor 'org.projectlombok:lombok'
}

tasks.named('test') {
	useJUnitPlatform()
}

// QueryDSL Q클래스 생성 위치
def querydslDir = "$buildDir/generated/querydsl"

sourceSets {
	main.java.srcDirs += [querydslDir]
}

tasks.withType(JavaCompile) {
	options.generatedSourceOutputDirectory = file(querydslDir)
}

clean.doLast {
	file(querydslDir).deleteDir()
}
```

### Q클래스 생성 순서

1. build.gradle 저장
2. `gradlew clean compileJava`
3. 프로젝트 우클릭 → **Gradle → Refresh Gradle Project**
4. `build/generated/querydsl`에 `Q*.java` 생성 확인 (Refresh 후 사라지면 `compileJava` 재실행)

---

## 배운 점

1. **에러 원인은 Gradle 콘솔/Problems 탭의 `error:` 줄에 있다.** 빌드 트리 스크린샷은 실패 여부만 보여준다.
2. **오타와 대소문자**가 가장 흔한 원인이었다 (`JPAQueryFactory`, `Colloge`, `setProfName`).
3. **엔티티 필드명을 바꾸면** DTO, Repository, Service, Q클래스까지 연결된 코드를 함께 수정해야 한다.
4. **쓰다 만 코드 한 줄**이 파일 전체에 연쇄 문법 에러를 만든다. 맨 위 에러부터 고친다.
5. Q클래스는 `compileJava` 때만 생성된다. 엔티티를 수정하면 다시 생성해야 한다.
6. 연관관계는 `@ManyToOne` + `@JoinColumn`으로 걸면 `student.dept1.dname`처럼 객체 경로로 조인할 수 있다.
7. 같은 테이블을 여러 번 조인하면 Q클래스 별칭을 따로 만든다.
