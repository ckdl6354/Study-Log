# 🚨 [JSP/MyBatis] 게시글 수정 데이터 미반영 및 톰캣 에러 해결 기동

## 1. 발생했던 에러 현상들
1. 수정화면 진입 후 새로고침 시 콘솔에 `java.lang.NumberFormatException: null` 발생 및 화면 먹통
2. 글 수정 완료 후 상세조회 페이지로 갔으나 수정한 글자가 화면에 출력 안 됨
3. 마이바티스 실행 시 `Unknown column 'image_filename' in 'field list'` 에러 발생

---

## 2. 진짜 원인 분석 (내가 놓쳤던 부분)
- **컨트롤러 흐름 오류**: `doPost`에서 글 수정을 처리한 뒤 주소창을 바꾸지 않고(`forward`), 최신 데이터도 조회하지 않은 채 JSP 파일로 바로 넘겨버려서 화면이 빈 통으로 나왔음.
- **HTML 파라미터 누락**: 수정 폼(`modifyform.jsp`)에서 글 번호(`num`)를 서버로 넘겨주는 `<input type="hidden">` 태그가 없어서 서버가 어떤 글을 고쳐야 할지 몰라 `null` 에러가 터짐.
- **MyBatis SQL 및 컬럼명 매칭 오류**: XML 매퍼 쿼리문 안에 세미콜론(`;`)이 들어가 오작동했고, DB의 실제 컬럼명은 `imageFilename`(카멜케이스)인데 XML에는 `image_filename`(스네이크케이스)으로 적어서 디비가 컬럼을 찾지 못함.

---

## 3. 완벽 해결 코드 정리

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
    WHERE num = #{num} <!-- **(중요!!) 세미콜론(;) 금지!** --> 
</update>
```

---

## 💡 오늘의 교훈 (복습 포인트)
1. **웹 - 컨트롤러 - DTO - 서비스 - DB** 흐름에서 데이터(`num` 등)가 중간에 끊기지 않고 계속 이어지는지 파라미터 확인을 제일 먼저 하자.
2. 디비 에러 `Unknown column`이 뜨면 자바 코드만 보지 말고 **진짜 DB 테이블의 컬럼명 철자**와 대소문자를 눈으로 꼭 확인하자!
