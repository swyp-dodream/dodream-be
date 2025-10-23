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


2. **환경 변수 설정**
프로젝트 루트에 `.env` 파일을 생성하고 다음 환경 변수들을 설정하세요:

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=dodream
DB_USERNAME=root
DB_PASSWORD=your_database_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Server Configuration
SERVER_PORT=8080

# OAuth Configuration
GOOGLE_CLIENT_ID=your_google_client_id_here
GOOGLE_CLIENT_SECRET=your_google_client_secret_here
NAVER_CLIENT_ID=your_naver_client_id_here
NAVER_CLIENT_SECRET=your_naver_client_secret_here

# JWT Configuration
JWT_SECRET=your_jwt_secret_here
```

**중요**: `.env` 파일은 절대 Git에 커밋하지 마세요. 민감한 정보가 포함되어 있습니다.

3. **MySQL 컨테이너 실행**
```bash
docker run --name dodream \
  -e MYSQL_ROOT_PASSWORD=dodream123 \
  -e MYSQL_DATABASE=dodream \
  -p 3306:3306 -d mysql:latest
```

4. **Redis 컨테이너 실행**
```bash
docker run --name dodream-redis \
  -p 6379:6379 -d redis:7-alpine
```

5. **애플리케이션 실행**
```bash
./gradlew bootRun
```

## 📊 ERD

[ERD Cloud 링크](https://www.erdcloud.com/d/fZNNYLXx5ggoB7DNe)

## 👥 팀원

- **김소희** - Server, 검색, 채팅
- **최현우** - 프로필 관리, 알림, AI 프로필 작성
- **조훈** - 로그인, 인프라, AI 매칭, API 문서화

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


