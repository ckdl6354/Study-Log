Gradle에서 jdk가 26으로 인식되고 11로 변경이안될때 Gradle 실행 JDK를 11로 지정 (핵심)
Window → Preferences → Gradle Java home에 JDK 11 경로 입력 (예: C:\Program Files\Java\jdk-11.0.xx) Apply and Close

이유 : Gradle가 버전이 JDK26으로 실행되서 JDK11을 못불러와서 직접 경로를 설정해줘야함
