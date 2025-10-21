# 🌟 DoDream Back

## 🛠 기술 스택

### Backend
- **언어** : Java 21
- **프레임워크** : Spring Boot 3.5.6, Spring Data JPA
- **DB** : MySQL, Redis
- **Server** : NCP

## 🚀 시작하기

### 사전 요구사항
- Java 21
- Docker (MySQL 컨테이너용)
- Google OAuth 2.0 Client ID & Secret

### 설치 및 실행

1. **프로젝트 클론**
```bash
git clone https://github.com/swyp-dodream/dodream-be.git
cd dodream
```

2. **환경변수 설정**
```bash
# Google OAuth 환경변수 설정
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret
```

3. **MySQL 컨테이너 실행**
```bash
docker run --name dodream \
  -e MYSQL_ROOT_PASSWORD=dodream123 \
  -e MYSQL_DATABASE=dodream \
  -p 3306:3306 -d mysql:latest
```

4. **애플리케이션 실행**
```bash
./gradlew bootRun
```

### Google OAuth 설정 방법

1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 생성
3. **API 및 서비스** → **사용자 인증 정보** → **사용자 인증 정보 만들기** → **OAuth 클라이언트 ID**
4. 애플리케이션 유형: **웹 애플리케이션**
5. 승인된 리디렉션 URI:
   - 로컬: `http://localhost:8080/login/oauth2/code/google`
   - 운영: `http://49.50.132.63:8080/login/oauth2/code/google`
6. Client ID와 Client Secret을 환경변수에 설정

## 📊 ERD

[ERD Cloud 링크](https://www.erdcloud.com/d/fZNNYLXx5ggoB7DNe)

## 👥 팀원

- **김소희** - Server, 검색, 채팅
- **최현우** - 로그인, 프로필 관리, 알림, AI 프로필 작성
- **조훈** - 인프라, AI 매칭, API 문서화

## 📝 API 문서

애플리케이션 실행 후 Swagger UI에서 API 문서를 확인할 수 있습니다.
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 🔧 주요 기능

- **OAuth 2.0 소셜 로그인** (Google, Naver)
- **AI 기반 프로젝트 매칭**
- **실시간 채팅**
- **실시간 알림**
- **프로필 검색**
- **AI 자기소개 작성 지원**


