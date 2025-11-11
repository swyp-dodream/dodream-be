# 🚀 로컬 개발 환경 설정 가이드

## 📋 목차
1. [사전 요구사항](#사전-요구사항)
2. [Docker 컨테이너 실행](#docker-컨테이너-실행)
3. [환경 변수 설정](#환경-변수-설정)
4. [MySQL 덤프 복원](#mysql-덤프-복원)
5. [애플리케이션 실행](#애플리케이션-실행)
6. [유용한 명령어](#유용한-명령어)

---

## 사전 요구사항

- Java 21
- Docker Desktop 설치 및 실행
- Git

---

## Docker 컨테이너 실행

### 1. MySQL 실행

```bash
docker run --name dodream-mysql \
  -e MYSQL_ROOT_PASSWORD=root123! \
  -e MYSQL_DATABASE=dodream \
  -p 3306:3306 \
  -d mysql:8.0
```

### 2. Redis 실행

```bash
docker run --name dodream-redis \
  -p 6379:6379 \
  -d redis:7-alpine
```

### 3. Qdrant 실행

```bash
docker run --name dodream-qdrant \
  -p 6333:6333 \
  -p 6334:6334 \
  -d qdrant/qdrant:latest
```

### 4. Elasticsearch 실행

```bash
docker run --name dodream-elasticsearch \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -p 9200:9200 \
  -p 9300:9300 \
  -d docker.elastic.co/elasticsearch/elasticsearch:8.17.4
```

### 5. 모든 컨테이너 한 번에 실행

```bash
# MySQL
docker run --name dodream-mysql \
  -e MYSQL_ROOT_PASSWORD=root123! \
  -e MYSQL_DATABASE=dodream \
  -p 3306:3306 \
  -d mysql:8.0

# Redis
docker run --name dodream-redis \
  -p 6379:6379 \
  -d redis:7-alpine

# Qdrant
docker run --name dodream-qdrant \
  -p 6333:6333 \
  -p 6334:6334 \
  -d qdrant/qdrant:latest

# Elasticsearch
docker run --name dodream-elasticsearch \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -p 9200:9200 \
  -p 9300:9300 \
  -d docker.elastic.co/elasticsearch/elasticsearch:8.17.4
```

### 컨테이너 상태 확인

```bash
docker ps
```

---

## 환경 변수 설정

프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 다음 내용을 추가하세요:

```env
# Database 설정
DB_HOST=localhost
DB_PORT=3306
DB_NAME=dodream
DB_USERNAME=root
DB_PASSWORD=root123!

# Redis 설정
REDIS_HOST=localhost
REDIS_PORT=6379

# Server 설정
SERVER_PORT=8080

# JWT 설정
JWT_SECRET=your-jwt-secret-key-here

# Google OAuth2 설정
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Naver OAuth2 설정
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret

# NCP Clova API 설정
NCP_CLOVA_API_KEY=your-ncp-clova-api-key

# NCP Clova Embedding 설정 (선택사항)
CLOVA_EMBEDDING_API_KEY=your-embedding-api-key
CLOVA_EMBEDDING_API_GW_KEY=your-embedding-api-gw-key
CLOVA_EMBEDDING_BASE_URL=https://clovastudio.stream.ntruss.com/v1/api-tools/embedding/v2
CLOVA_EMBEDDING_MODEL=nlp-embedding-ko-1.0.1
```

> ⚠️ **주의**: 실제 값으로 변경해야 합니다. 민감한 정보는 절대 Git에 커밋하지 마세요.

---

## MySQL 덤프 복원

### 방법 1: 서버에서 덤프 파일 다운로드 후 복원 (권장)

#### 1단계: 서버에서 덤프 파일 생성

서버에 SSH 접속 후:

```bash
ssh -i ~/Downloads/dodream.pem root@49.50.132.63

# 서버에서 MySQL 덤프 생성
docker exec dodream-mysql mysqldump -u root -proot123! dodream > /tmp/dodream_dump.sql
```

#### 2단계: 로컬로 파일 다운로드

로컬 터미널에서:

```bash
scp -i ~/Downloads/dodream.pem root@49.50.132.63:/tmp/dodream_dump.sql ./dodream_dump.sql
```

#### 3단계: 로컬 MySQL에 덤프 복원

```bash
docker exec -i dodream-mysql mysql -u root -proot123! dodream < dodream_dump.sql
```

### 방법 2: 직접 파이프로 전송 (원라이너)

```bash
ssh -i ~/Downloads/dodream.pem root@49.50.132.63 \
  "docker exec dodream-mysql mysqldump -u root -proot123! dodream" | \
  docker exec -i dodream-mysql mysql -u root -proot123! dodream
```

### 방법 3: Docker 컨테이너에 직접 접속하여 작업

```bash
# 1. 덤프 파일을 컨테이너에 복사
docker cp dodream_dump.sql dodream-mysql:/tmp/dump.sql

# 2. 컨테이너 내부에서 복원
docker exec -it dodream-mysql bash
mysql -u root -proot123! dodream < /tmp/dump.sql
exit
```

---

## 애플리케이션 실행

### 1. 프로젝트 빌드

```bash
./gradlew build
```

### 2. 애플리케이션 실행

#### 개발 환경으로 실행 (권장)

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### 기본 실행

```bash
./gradlew bootRun
```

### 3. 애플리케이션 확인

- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

---

## 유용한 명령어

### Docker 컨테이너 관리

#### 컨테이너 중지

```bash
docker stop dodream-mysql dodream-redis dodream-qdrant dodream-elasticsearch
```

#### 컨테이너 시작

```bash
docker start dodream-mysql dodream-redis dodream-qdrant dodream-elasticsearch
```

#### 컨테이너 재시작

```bash
docker restart dodream-mysql dodream-redis dodream-qdrant dodream-elasticsearch
```

#### 컨테이너 삭제 (⚠️ 주의: 데이터 삭제됨)

```bash
docker rm -f dodream-mysql dodream-redis dodream-qdrant dodream-elasticsearch
```

### 로그 확인

```bash
# MySQL 로그
docker logs dodream-mysql

# Redis 로그
docker logs dodream-redis

# Qdrant 로그
docker logs dodream-qdrant

# Elasticsearch 로그
docker logs dodream-elasticsearch

# 실시간 로그 확인 (tail -f)
docker logs -f dodream-mysql
```

### 컨테이너 내부 접속

#### MySQL 접속

```bash
# Bash 접속
docker exec -it dodream-mysql bash

# MySQL CLI 접속
docker exec -it dodream-mysql mysql -u root -proot123! dodream
```

#### Redis 접속

```bash
docker exec -it dodream-redis redis-cli
```

#### Qdrant 확인

```bash
# HTTP API로 컬렉션 목록 확인
curl http://localhost:6333/collections

# Qdrant Dashboard (브라우저)
# http://localhost:6333/dashboard
```

#### Elasticsearch 확인

```bash
# 클러스터 상태 확인
curl http://localhost:9200

# 인덱스 목록 확인
curl http://localhost:9200/_cat/indices?v
```

### 데이터베이스 작업

#### MySQL 데이터베이스 목록 확인

```bash
docker exec -it dodream-mysql mysql -u root -proot123! -e "SHOW DATABASES;"
```

#### MySQL 테이블 목록 확인

```bash
docker exec -it dodream-mysql mysql -u root -proot123! dodream -e "SHOW TABLES;"
```

#### MySQL 덤프 생성 (로컬)

```bash
docker exec dodream-mysql mysqldump -u root -proot123! dodream > local_dump.sql
```

---

## 포트 정보

| 서비스 | 포트 | 설명 |
|--------|------|------|
| MySQL | 3306 | 데이터베이스 |
| Redis | 6379 | 캐시 및 세션 저장소 |
| Qdrant | 6333 | HTTP API |
| Qdrant | 6334 | gRPC API |
| Elasticsearch | 9200 | HTTP API |
| Elasticsearch | 9300 | Transport API |
| Spring Boot | 8080 | 애플리케이션 서버 |

---

## 전체 설정 순서 요약

```bash
# 1. 모든 Docker 컨테이너 실행
docker run --name dodream-mysql -e MYSQL_ROOT_PASSWORD=root123! -e MYSQL_DATABASE=dodream -p 3306:3306 -d mysql:8.0
docker run --name dodream-redis -p 6379:6379 -d redis:7-alpine
docker run --name dodream-qdrant -p 6333:6333 -p 6334:6334 -d qdrant/qdrant:latest
docker run --name dodream-elasticsearch -e "discovery.type=single-node" -e "xpack.security.enabled=false" -p 9200:9200 -p 9300:9300 -d docker.elastic.co/elasticsearch/elasticsearch:8.17.4

# 2. 컨테이너 상태 확인
docker ps

# 3. .env 파일 생성 및 환경 변수 설정
# (프로젝트 루트에 .env 파일 생성)

# 4. 서버에서 덤프 다운로드 (선택사항)
scp -i ~/Downloads/dodream.pem root@49.50.132.63:/tmp/dodream_dump.sql ./dodream_dump.sql

# 5. 로컬 MySQL에 덤프 복원 (덤프가 있는 경우)
docker exec -i dodream-mysql mysql -u root -proot123! dodream < dodream_dump.sql

# 6. 애플리케이션 빌드 및 실행
./gradlew build
./gradlew bootRun --args='--spring.profiles.active=dev'
```

---

## 문제 해결

### 포트 충돌 오류

이미 사용 중인 포트가 있는 경우:

```bash
# 포트 사용 확인
lsof -i :3306
lsof -i :6379
lsof -i :6333
lsof -i :9200

# 기존 컨테이너 확인
docker ps -a

# 기존 컨테이너 삭제 후 재생성
docker rm -f dodream-mysql
```

### MySQL 연결 오류

```bash
# MySQL 컨테이너 로그 확인
docker logs dodream-mysql

# MySQL 컨테이너 재시작
docker restart dodream-mysql

# MySQL 접속 테스트
docker exec -it dodream-mysql mysql -u root -proot123! -e "SELECT 1;"
```

### Elasticsearch 시작 실패

Elasticsearch는 메모리를 많이 사용합니다. Docker Desktop의 메모리 할당량을 확인하세요.

```bash
# Elasticsearch 로그 확인
docker logs dodream-elasticsearch

# 메모리 부족 시 재시작
docker restart dodream-elasticsearch
```

---

## 참고 자료

- [Docker 공식 문서](https://docs.docker.com/)
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)
- [Redis Docker Hub](https://hub.docker.com/_/redis)
- [Qdrant 공식 문서](https://qdrant.tech/documentation/)
- [Elasticsearch 공식 문서](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)

