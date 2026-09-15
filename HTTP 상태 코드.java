#  JSP/Servlet 핵심 에러 모음

JSP 및 웹 개발 시 가장 자주 마주치는 **HTTP 상태 코드**와 **자바 예외(Exception) 문자열** 정리입니다. 
이 문자열들을 외워두면 에러가 발생했을 때 해결 속도가 획기적으로 빨라집니다.

---

## 1. 브라우저에 표시되는 HTTP 상태 코드

### 404 Not Found (경로 오류)
* **의미:** 요청한 페이지(JSP, HTML 등)를 찾을 수 없음.
* **주요 원인:**
  * URL 주소창에 오타가 발생한 경우
  * 파일명이 대소문자까지 똑같지 않은 경우
  * 파일을 `WebContent` 또는 `webapp` 폴더가 아닌 `WEB-INF` 내부 등 잘못된 경로에 넣은 경우

###  405 Method Not Allowed (요청 방식 오류)
* **의미:** 클라이언트가 요청한 방식(GET 또는 POST)을 서버가 지원하지 않음.
* **주요 원인:**
  * JSP `<form>` 태그에서 `method="post"`로 전송했으나, 받는 서블릿(Servlet)에 `doPost()` 메서드가 구현되어 있지 않은 경우

###  500 Internal Server Error (서버 내부 오류)
* **의미:** 웹 서버가 요청을 처리하는 과정에서 자바 코드가 터짐.
* **주요 원인:**
  * JSP 파일 내 스크립틀릿(`<% %>`) 코드의 문법 오류
  * 서블릿 소스 코드 실행 중 예외(Exception) 발생
  *  *브라우저 화면뿐만 아니라 이클립스/톰캣 콘솔(Console) 로그를 즉시 확인해야 합니다.*

---

##  2. 이클립스 / 톰캣 콘솔에 찍히는 자바 예외 (Exception)

###  `java.lang.NullPointerException` (NPE)
* **의미:** 값이 비어있는(`null`) 객체의 메서드를 호출하거나 속성에 접근하려고 할 때 발생.
* **JSP 예시:** 
  ```java
  String name = request.getParameter("username"); // 사용자가 값을 안 적어서 null이 넘어옴
  if (name.equals("admin")) { ... } // null.equals()가 실행되면서 NPE 발생!
  ```

###  `java.lang.ClassNotFoundException`
* **의미:** 필요한 자바 클래스 파일(`.class`)이나 라이브러리(`.jar`)를 빌드 경로에서 찾을 수 없음.
* **JSP 예시:** 데이터베이스(DB) 연동 시 JDBC 드라이버 파일(예: `ojdbc.jar`, `mysql-connector.jar`)을 `src/main/webapp/WEB-INF/lib` 폴더에 넣지 않았을 때 백퍼센트 발생합니다.

###  `org.apache.jasper.JasperException`
* **의미:** 톰캣(Jasper 엔진)이 JSP 파일을 서블릿(자바 파일)으로 변환(컴파일)하다가 실패함.
* **주요 원인:**
  * JSP 안에서 스크립틀릿 괄호(`<%` 또는 `%>`)를 제대로 닫지 않은 경우
  * 커스텀 태그나 지시자(`<%@ page ... %>`) 선언 문법 오류

###  `java.sql.SQLException`
* **의미:** 데이터베이스(DB) 작업 도중 오류가 발생함.
* **주요 원인:**
  * SQL 쿼리문 안의 오타 (컬럼명, 테이블명 오타 등)
  * DB 서버가 꺼져 있거나 연결 정보(ID/PW, URL)가 잘못된 경우

---

## 💡 에러 디버깅 꿀팁
이클립스 콘솔에 수십 줄의 붉은색 에러 로그(Stack Trace)가 찍히면 당황하지 말고 **`Caused by: ...`** 문자열을 찾으세요. 그 뒷부분에 에러가 시작된 **진짜 원인**과 **정확한 코드 라인 번호**가 적혀 있습니다!
