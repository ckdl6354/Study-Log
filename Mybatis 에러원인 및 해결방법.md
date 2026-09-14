# ❌ [Troubleshooting] MyBatis DB 연동 에러: SQLInvalidAuthorizationSpecException (Access denied)

MyBatis 프레임워크 기반 프로젝트에서 데이터베이스(MySQL/MariaDB) 접속을 시도할 때 발생한 **`Access denied` (접근 거부)** 에러의 원인과 해결 과정을 정리한 기록입니다.

---

## 1. 에러 로그 (Error Log)
애플리케이션 구동 중 데이터베이스 쿼리를 실행하는 시점에 아래와 같은 예외가 발생하며 시스템이 정상 작동하지 않았습니다.

```text
Caused by: java.sql.SQLInvalidAuthorizationSpecException: (conn=49) Access denied for user 'root'@'localhost' (using password: YES)
### Error querying database.  Cause: java.sql.SQLInvalidAuthorizationSpecException: (conn=49) Access denied for user 'root'@'localhost' (using password: YES)
### The error may exist in mapper/member.xml
### The error may involve mapper.member.selectMember
### The error occurred while executing a query
```

---

## 2. 발생 원인 (Root Cause)
오류 메시지의 **`Access denied for user 'root'@'localhost' (using password: YES)`**를 통해 다음과 같은 두 가지 원인을 추려낼 수 있습니다.

1. **설정 파일 내 비밀번호 불일치 (가장 유력):** `mybatis-config.xml`에 적힌 `password` 값이 현재 로컬 컴퓨터(PC)에 설치된 실제 MySQL `root` 계정의 비밀번호와 일치하지 않음.
2. **로컬호스트 접속 권한 부재:** MySQL 내부에서 `root` 계정이 로컬 환경(`localhost`)으로부터 접속할 수 있는 권한이 차단되었거나 유실됨.

> 💡 **참고:** 에러 로그에 `mapper/member.xml`이 언급되는 이유는 해당 XML 파일 내의 SQL 쿼리를 실행하는 과정에서 데이터베이스 커넥션을 맺으려다 인증에 실패했기 때문입니다. 즉, **SQL 쿼리문 자체에는 문제가 없습니다.**

---

## 3. 해결 과정 및 방법 (Solution Steps)

### ➔ 1단계: 메인 설정 파일(`mybatis-config.xml`) 위치 추적 및 수정
프로젝트 내부 구조를 파악한 결과, `config.MybatisSqlSessionFactory` 자바 클래스 파일에서 메인 DB 설정 파일로 `resource/mybatis-config.xml`을 로드하고 있음을 확인했습니다.

```java
// config/MybatisSqlSessionFactory.java 내 설정 파일 로드 소스
String resource = "resource/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
```

이 경로를 바탕으로 **`src/main/resources/resource/mybatis-config.xml`** 파일을 열어 데이터베이스 접속 정보를 수정했습니다.

```xml
<!-- mybatis-config.xml 수정 전/후 비교 -->
<dataSource type="POOLED">
    <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/YOUR_DATABASE"/>
    <property name="username" value="root"/>
    
    <!-- 🚨 [수정 전] 기존의 잘못되었던 비밀번호 혹은 비어있던 값 -->
    <!-- <property name="password" value="wrong_password"/> -->
    
    <!-- ✅ [수정 후] 실제 로컬 PC의 MySQL root 비밀번호 입력 -->
    <property name="password" value="YOUR_ACTUAL_PASSWORD"/> 
</dataSource>
```

### ➔ 2단계: 로컬 DB 계정 접속 권한 갱신 (선택 사항)
만약 설정 파일의 비밀번호를 올바르게 고쳤음에도 동일한 에러가 지속된다면, MySQL CLI 또는 DB 관리 도구(DBeaver, Workbench 등)에 접속하여 권한을 다시 부여해야 합니다.

```sql
-- 1. 현재 사용자 계정 및 호스트 권한 정보 조회
SELECT User, Host FROM mysql.user;

-- 2. root 계정에 localhost 접속 모든 권한 부여 (MySQL 8.0 미만)
GRANT ALL PRIVILEGES ON *.* TO 'root'@'localhost' IDENTIFIED BY '실제비밀번호';

-- 3. root 계정 비밀번호 및 권한 강제 재설정 (MySQL 8.0 이상)
ALTER USER 'root'@'localhost' IDENTIFIED BY '실제비밀번호';

-- 4. 변경된 설정 사항 즉시 반영
FLUSH PRIVILEGES;
```

### ➔ 3단계: WAS 서버 Clean 및 재시작 (Restart)
XML 및 자바 설정 파일의 변경 사항은 WAS에 즉시 반영되지 않을 수 있으므로 인메모리 데이터를 초기화해 줍니다.
1. Eclipse/STS 하단의 **`Servers`** 탭을 확인합니다.
2. 실행 중인 Tomcat 서버를 마우스 우클릭 후 **`Clean...`**을 진행합니다.
3. 서버를 **`Restart`** 하여 수정된 데이터베이스 인증 정보로 다시 커넥션 풀을 빌드합니다.

---

## 4. 요약 
* MyBatis 프로젝트 환경에서 DB 인증 오류가 발생했을 때 SQL 문이 담긴 개별 `Mapper.xml`을 보는 것은 해결책이 되지 않습니다.
* 커넥션을 직접 생성하고 관리하는 **`mybatis-config.xml`**, **`application.properties`**, 혹은 **데이터베이스 설정용 Java 파일**을 가장 먼저 추적해야 합니다.
* 비밀번호에 특수문자가 포함될 경우 설정 파일 형식에 따라 문자열 가공(예: 따옴표 감싸기 등)이 필요할 수 있으므로 각별히 주의해야 합니다.
