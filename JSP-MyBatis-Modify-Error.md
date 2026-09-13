# 🛠️ [JSP/MyBatis] 게시글 수정 데이터 미반영 및 톰캣 에러 해결기

## 1. 문제 상황 (Issue)
게시글 수정 기능을 구현하던 중 다음과 같은 3가지 연쇄 에러가 발생했습니다.
- **현상 1:** 수정 화면 진입 후 새로고침 시 콘솔에 `java.lang.NumberFormatException: null` 발생 및 화면 먹통
- **현상 2:** 글 수정 완료 후 상세조회 페이지로 이동했으나, 수정한 글자가 화면에 반영되지 않음
- **현상 3:** 마이바티스 실행 시 `Unknown column 'image_filename' in 'field list'` 에러 발생

---

## 2. 원인 분석 (Cause)
- **HTML 파라미터 누락:** 수정 폼(`modifyform.jsp`)에서 글 번호(`num`)를 서버로 넘겨주는 `<input type="hidden">` 태그가 없었습니다. 이로 인해 서버가 어떤 글을 고쳐야 할지 몰라 `null` 관련 포맷 에러가 터졌습니다.
- **컨트롤러 흐름 오류:** `doPost`에서 글 수정을 처리한 뒤 주소창을 바꾸지 않고(`forward`), 최신 데이터도 조회하지 않은 채 JSP 파일로 바로 넘겨버려서 화면에 빈 통 데이터가 나왔습니다.
- **MyBatis SQL 및 컬럼명 매칭 오류:** DB의 실제 컬럼명은 `imageFilename`(카멜케이스)인데 XML에는 `image_filename`(스네이크케이스)으로 적어서 DB가 컬럼을 찾지 못했습니다. 추가로 XML 매퍼 쿼리문 안에 세미콜론(`;`)이 들어가 오작동했습니다.

---

## 3. 해결 방법 (Solution)

### ① modifyform.jsp (hidden 태그 추가)
```html
<form action="modify" method="post" enctype="multipart/form-data">
    <!-- 글 번호를 무조건 서버로 같이 전송해야 함 -->
    <input type="hidden" name="num" value="\${article.num}">
    ...
</form>
```

### ② ModifyArticle.java (doPost 최신 데이터 재조회)
```java
try {
    service.modifyArticle(article, uploadPath, ifile, dfile);
    
    // 수정이 끝나면 무조건 최신 데이터를 다시 조회해서 request에 심어 보내야 화면에 출력됨!
    Article updatedArticle = service.detailArticle(article.getNum());
    request.setAttribute("article", updatedArticle); 
    
    request.getRequestDispatcher("/article/boarddetail.jsp").forward(request, response);
}
```

### ③ article.xml (마이바티스 매퍼 수정)
```xml
<update id="updateArticle" parameterType="article">
    UPDATE article 
    SET 
        title = #{title},
        content = #{content}
        <!-- DB 실제 컬럼명인 imageFilename으로 일치시켜야 에러 안 남 -->
        <if test="imageFilename != null and imageFilename != ''">
            , imageFilename = #{imageFilename}
        </if>
    WHERE num = #{num} <!-- (중요!!) 쿼리문 끝에 세미콜론(;) 금지 --> 
</update>
```

---

## 4. 회고 (Retrospective)
- **데이터 흐름 추적:** 웹(JSP) ➡️ 컨트롤러(Servlet) ➡️ 서비스/DTO ➡️ DB로 이어지는 파이프라인에서 데이터(`num` 등 키값)가 끊기지 않고 잘 전달되는지 검증하는 것이 디버깅의 최우선 순위임을 배웠습니다.
- **DB 명명 규칙 확인:** `Unknown column` 에러가 발생하면 자바 변수명만 보지 말고, 실제 DB 테이블의 컬럼명 철자와 대소문자(카멜/스네이크)를 매퍼와 교차 검증하는 습관을 들여야겠습니다.
