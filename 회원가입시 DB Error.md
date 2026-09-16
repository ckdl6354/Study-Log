# 트러블슈팅: 회원가입 시 DB Insert Error (emp_no Null 예외)

## 1. 문제 상황 (Issue)
* **상황**: 로그인 화면에서 회원가입 페이지로 이동(GET)한 뒤, 정보를 입력하고 가입 버튼을 눌러 제출(POST)했을 때 서버 내부에서 데이터베이스 저장 에러가 발생하며 가입이 실패함.
* **에러 로그**:
  ```text
  org.apache.ibatis.exceptions.PersistenceException: 
  ### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: (conn=58) Column 'emp_no' cannot be null
  ### The error may involve mapper.employee.insertEmployee-Inline
  ### SQL: insert into employee(emp_no,pw,emp_nm) values(?,?,?)
  ```

## 2. 원인 분석 (Cause)
* 데이터베이스 `employee` 테이블의 사번(`emp_no`) 컬럼은 필수 입력 값인 **`NOT NULL` 제약 조건**이 걸려 있음.
* 클라이언트(JSP 화면)에서 입력한 사번 데이터가 서버 컨트롤러(`SignUp.java`) 및 MyBatis 매퍼(`insertEmployee`)로 전달되는 과정에서 값이 유실되어 **`null` 상태로 쿼리가 실행**되었기 때문에 데이터 무결성 제약 조건 위배 예외가 발생함.

## 3. 해결 방법 (Solution)
데이터가 유실될 수 있는 3개 구간의 변수명 및 속성을 전수 조사하여 대소문자와 바인딩 설정을 일치시켰습니다.

1. **JSP 입력 폼 검증**: `<input>` 태그에 서버로 데이터를 전송하기 위한 고유 `name` 속성이 올바르게 채워져 있는지 확인하고 수정함.
   ```html
   <!-- name="empNo" 속성이 컨트롤러의 파라미터명과 일치해야 함 -->
   <input type="text" name="empNo" placeholder="사번" required maxlength="5" autofocus>
   ```
2. **Controller 서블릿 수정**: `SignUp.java`의 `doPost` 메서드에서 JSP의 `name` 값과 완벽히 대소문자를 맞춰 값을 꺼낸 뒤 DTO 객체에 세팅함.
   ```java
   String empNo = request.getParameter("empNo"); // 대소문자 일치 확인
   Employee emp = new Employee();
   emp.setEmpNo(empNo); // DTO 객체에 누락 없이 세팅
   ```
3. **MyBatis 매퍼 XML 확인**: `#{}` 자리에 선언된 파라미터명이 자바 DTO 필드명인 `empNo`와 대소문자까지 일치하는지 검증 후 톰캣 서버를 **Clean 및 재시작(Restart)**하여 최종 반영함.

## 4. 회고 (Retrospective)
* **데이터 흐름의 중요성**: 화면(JSP) ➔ 컨트롤러(Servlet) ➔ 매퍼(MyBatis) ➔ 데이터베이스(DB)로 이어지는 연동 과정에서 변수명 하나만 틀려도 데이터가 유실(Null)될 수 있음을 재확인
* **재발 방지**: 앞으로 HTML 요소를 제어할 때는 `id` 속성뿐만 아니라 서버 전송용인 `name` 속성을 빠뜨리지 않았는지 명확히 더블 체크하고, 수정 후에는 컴파일 찌꺼기가 남지 않도록 항상 이클립스/톰캣의 **Project Clean** 작업을 습관화 필요!
