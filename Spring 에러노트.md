
문제상황 : Gradle에서 jdk가 26으로 인식되고 11로 변경이 안됨

해결방법 : Gradle 실행 JDK를 11로 지정 (핵심)
Window → Preferences → Gradle Java home에 JDK 11 경로 입력 (예: C:\Program Files\Java\jdk-11.0.xx) Apply and Close

회고 : Preferences → java -> installed JREs에서 JDK 11로 뜨는지 확인후 설정이 안나오면
따로 위에있는 설정을 해줘야한다.
