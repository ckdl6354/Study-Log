## 회원가입 시 DB Insert Error (emp_no Null 예외)

### 에러 현상 (Issue)
- **에러 종류**: `org.apache.ibatis.exceptions.PersistenceException`
- **예외 내용**: `java.sql.SQLIntegrityConstraintViolationException: Column 'emp_no' cannot be null`
- **발생 위치**: `dao.EmployeeDaoImpl.insertEmployee` ➔ MyBatis 매퍼 `insertEmployee` 구문 실행 시 발생

### 원인 분석
- 데이터베이스 `employee` 테이블의 `emp_no` 컬럼은 필수(`NOT NULL`) 값이지만, 회원가입 데이터 제출(`POST`) 과정에서 서버 및 MyBatis 매퍼로 데이터가 정상 전달되지 않고 `null`로 바인딩됨.

### 해결 및 체크리스트
1. **JSP Form 검증**: 회원가입 입력창 `<input>` 태그의 `name="empNo"` 속성이 누락되었거나 오타가 있는지 확인 및 수정.
2. **Controller 매핑**: `SignUp.java` 서블릿에서 `request.getParameter("empNo")` 대소문자 일치 여부 및 DTO 객체(`setEmpNo`) 세팅 누락 수정.
3. **MyBatis Mapper 확인**: 매퍼 XML의 쿼리문 파라미터 `#{empNo}`와 DTO 필드명이 완벽히 일치하도록 수정.
