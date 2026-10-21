# Study-Log / Spring 에러노트.md

### 🔍 문제상황
* Gradle에서 jdk가 **26**으로 인식되고 **11**로 변경이 안 됨

### 💡 해결방법
* **Gradle 실행 JDK를 11로 지정 (핵심)** 
* `Window` → `Preferences` → `Gradle` → `Java home`에 **JDK 11 경로 입력**
  * *예시: `C:\Program Files\Java\jdk-11.0.xx`*
* `Apply and Close` 선택

### 📝 회고
* `Preferences` → `java` → `installed JREs`에서 JDK 11로 뜨는지 확인해야 합니다.
* 만약 설정이 안 나온다면 따로 위에 있는 설정을 진행해 주어야 합니다.
