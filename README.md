<img alt="image" src="https://github.com/user-attachments/assets/ae92c664-4b27-4610-b9e7-bb80488a17f7" alt="image" width="900" />

# 🌟 DoDream Backend

## 🎯 프로젝트 목적
프로젝트 매칭 서비스를 위한 백엔드 API 서버입니다.

AI 기반 추천 시스템을 통해 사용자에게 맞는 프로젝트/스터디를 추천하고, 실시간 채팅과 알림 기능을 제공합니다.

### 핵심 목표
- **맞춤형 매칭**: AI 기반 추천 시스템으로 사용자 프로필과 게시글의 의미적 유사도를 계산하여 적합한 매칭 제공
- **효율적인 협업**: 실시간 채팅과 알림 시스템으로 원활한 소통 환경 구축
- **편리한 검색**: Elasticsearch를 활용한 강력한 검색 기능으로 원하는 프로젝트/스터디 빠르게 발견
- **안전한 인증**: OAuth2와 JWT를 활용한 안전하고 편리한 인증 시스템

## 💡 기대효과

### 사용자 관점
-  **시간 절약**: AI 추천으로 수동 검색 시간 대폭 감소
-  **정확한 매칭**: 의미 기반 유사도 계산으로 단순 키워드 매칭보다 정확한 추천
-  **편리한 협업**: 실시간 채팅과 알림으로 효율적인 팀 커뮤니케이션
-  **다양한 선택지**: 프로젝트/스터디 타입별 필터링으로 원하는 형태의 협업 발견

### 기술적 관점
-  **고성능 검색**: Elasticsearch와 벡터 검색을 통한 빠른 검색 성능
-  **안정적인 인증**: JWT 자동 재발급으로 끊김 없는 사용자 경험
-  **분산 환경 지원**: Redis Pub/Sub을 통한 채팅 시스템의 수평 확장 가능

### 비즈니스 관점
-  **사용자 만족도 향상**: 맞춤형 추천으로 사용자 이탈률 감소
-  **플랫폼 활성화**: 효율적인 매칭으로 더 많은 협업 성사
-  **데이터 축적**: 사용자 행동 데이터를 통한 추천 시스템 지속적 개선

## 🙋‍♂️ 개발자들 및 역할
- **조훈** : Oauth2 (GOOGLE,NAVER) 로그인, CI-CD, 서버배포 및 관리, AI매칭
- **김소희** : 모집글, 필터 및 검색(ES), 실시간 채팅(WebSocket + STOMP)
- **최현우** : 프로필, 알람(SSE, Redis Pub/Sub), AI 자기소개 초안 생성
  

## 🕒 프로젝트 기간

2025.10.04 ~ 2025.11.29 **(2개월)** 

## ✨ 주요 기능

### 🔐 구글, 네이버 로그인
### 🤖 AI 기반 추천 시스템
### 💼 지원 및 매칭
### 💬 실시간 채팅
### 🔔 알림 시스템
### 🔍 검색


## 🛠 기술 스택

### Backend

![Java](https://img.shields.io/badge/☕_Java-F89820?&style=for-the-badge&logo=Java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?&style=for-the-badge&logo=SpringBoot&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?&style=for-the-badge&logo=JUnit5&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?&style=for-the-badge&logo=Gradle&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?&style=for-the-badge&logo=Swagger&logoColor=black)


### Database
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-FF0000?style=for-the-badge&logo=Redis&logoColor=white)


### AI
![Clova](https://img.shields.io/badge/Clova-6DB33F?&style=for-the-badge&logo=Clova&logoColor=white)
![Qdrant](https://img.shields.io/badge/Qdrant-FF0000?style=for-the-badge&logo=Qdrant&logoColor=white)

### Search Engine
![ElasticSearch](https://img.shields.io/badge/ElasticSearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)

### DevOps & Infrastructure
![NGINX](https://img.shields.io/badge/NGINX-009639?style=for-the-badge&logo=nginx&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

### Others
![WebSocket](https://img.shields.io/badge/WebSocket-009639?style=for-the-badge&logo=WebSocket&logoColor=white)
![Stomp](https://img.shields.io/badge/Stomp-2496ED?style=for-the-badge&logo=Stomp&logoColor=white)



## 🚢 배포

### 서버 아키텍처
#### 배포 URL: https://dodream.store
<img src="https://github.com/user-attachments/assets/224fd0be-1160-4ef1-9a3b-69b367a8eb79"
     alt="image"
     width="500" />


## 📊 ERD
[ERD Cloud 링크](https://www.erdcloud.com/d/MWtoCFWtryAdgCrYc)


## 🔗 관련 링크
[프론트엔드 저장소](https://github.com/swyp-dodream/dodream-fe)

