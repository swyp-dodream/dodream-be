# DoDream 전체 플로우 테스트 가이드

## 사전 준비사항

✅ Docker 컨테이너 확인 완료
- MySQL: `dodream` 컨테이너 실행 중 (포트 3306)
- Redis: `dodream-redis` 컨테이너 실행 중 (포트 6379)
- Qdrant: `dodream-qdrant` 컨테이너 실행 중 (포트 6333-6334)

✅ 마스터 데이터 확인 완료
- Role (직군): 7개 (백엔드 개발자, 프론트엔드 개발자, iOS 개발자, 안드로이드 개발자, 디자이너, PM, 데브옵스)
- TechSkill (기술 스택): 7개 (Java, Kotlin, MySQL, Next.js, React, Spring Boot, Swift)
- InterestKeyword (관심 키워드): 4개 (AI, 머신러닝, 웹개발, 스타트업)

✅ 애플리케이션 실행 확인
- 서버: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html

## 테스트 순서

### 1. 구글 OAuth2 로그인

브라우저에서 다음 URL을 열어주세요:
```
http://localhost:8080/oauth2/authorization/google
```

**로그인 후 토큰 확인 방법:**
- 브라우저 개발자 도구 > Network 탭 > 리다이렉트된 응답 확인
- 또는 OAuth2SuccessHandler에서 반환되는 JWT 토큰 확인
- 일반적으로 `/login/oauth2/code/google?token=...` 형태로 리다이렉트됩니다

**토큰을 복사해서 아래 테스트에 사용하세요.**

### 2. 프로필 생성 테스트

```bash
curl -X POST http://localhost:8080/api/profiles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMTAzNjY2NzU1MDQ3MzAxMTIiLCJlbWFpbCI6ImpvaG9vbjAzMEBnbWFpbC5jb20iLCJuYW1lIjoiSG9vbiBDaG8gKEpvaG5leSkiLCJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNzYyMDAzNDQxLCJleHAiOjE3NjIwMDUyNDF9.lBtjpDcQyb08AJerHev7zm0I3NoSxCJgF_9YfDP3he22YfsUTSokt-vtO-Hpx51pe9rsc2-vVQEGBAm3-4AwsA" \
  -d '{
    "nickname": "테스트유저",
    "gender": "남성",
    "ageBand": "이십대",
    "experience": "신입",
    "activityMode": "온라인",
    "profileImageCode": 1,
    "roleNames": ["백엔드 개발자"],
    "interestKeywordNames": ["AI", "웹개발"],
    "techSkillNames": ["Java", "Spring Boot"],
    "introText": "테스트 프로필입니다.",
    "projectProposalEnabled": true,
    "studyProposalEnabled": true,
    "profileUrls": {
      "깃허브": "https://github.com/testuser"
    }
  }'
```

**예상 결과:**
- ✅ 프로필 생성 성공
- ✅ Role, InterestKeyword, TechSkill 정상 조회 (findByNameInWithCollation 사용)
- ✅ 프로필 ID 반환

### 3. 게시글 생성 테스트

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMTAzNjY2NzU1MDQ3MzAxMTIiLCJlbWFpbCI6ImpvaG9vbjAzMEBnbWFpbC5jb20iLCJuYW1lIjoiSG9vbiBDaG8gKEpvaG5leSkiLCJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNzYyMDA5MDkwLCJleHAiOjE3NjIwMTA4OTB9.1u8QgQ6Lq5GYtxvDb7-Hd7p8T6ijGCKXxmxro1kHsj_HlRh97sKQQc2bNDaEUNHh3squxKECkd5LmXZuZdpKJw" \
  -d '{
    "title": "백엔드 개발자 모집합니다",
    "content": "Spring Boot와 Java를 사용하는 프로젝트에 백엔드 개발자를 모집합니다. 신입도 환영합니다.",
    "projectType": "PROJECT",
    "status": "RECRUITING",
    "activityMode": "ONLINE",
    "duration": "THREE_MONTHS",
    "deadlineAt": "2025-12-31T23:59:59",
    "roles": [
      {
        "roleId": 1103543170005770241,
        "count": 2
      }
    ],
    "stackIds": [1, 2],
    "categoryIds": [1]
  }'
```

**예상 결과:**
- ✅ 게시글 생성 성공
- ✅ 게시글 임베딩 생성 및 Qdrant 저장
- ✅ Qdrant에서 벡터 확인 가능

### 4. 임베딩 저장 확인

Qdrant에 벡터가 저장되었는지 확인:

```bash
# Qdrant HTTP API로 확인
curl http://localhost:6333/collections/posts_embeddings | jq
```

또는 직접 Docker로 접속:

```bash
docker exec dodream-qdrant curl http://localhost:6333/collections/posts_embeddings
```

### 5. 게시글 추천 API 테스트

```bash
curl -X GET "http://localhost:8080/api/recommendations?size=5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMTAzNjY2NzU1MDQ3MzAxMTIiLCJlbWFpbCI6ImpvaG9vbjAzMEBnbWFpbC5jb20iLCJuYW1lIjoiSG9vbiBDaG8gKEpvaG5leSkiLCJ0eXBlIjoiYWNjZXNzIiwiaWF0IjoxNzYyMDA5MDkwLCJleHAiOjE3NjIwMTA4OTB9.1u8QgQ6Lq5GYtxvDb7-Hd7p8T6ijGCKXxmxro1kHsj_HlRh97sKQQc2bNDaEUNHh3squxKECkd5LmXZuZdpKJw"
```

**예상 결과:**
- ✅ 사용자 프로필 기반 벡터 유사도 계산
- ✅ Qdrant에서 유사한 게시글 검색
- ✅ 추천 게시글 목록 반환 (similarity 점수 포함)

### 6. Swagger UI에서 테스트

더 편리한 테스트를 위해 Swagger UI 사용:

1. 브라우저에서 열기: http://localhost:8080/swagger-ui/index.html
2. 우측 상단 "Authorize" 버튼 클릭
3. JWT 토큰 입력 (Bearer 제외)
4. 각 API 테스트 실행

## 문제 해결

### 프로필 생성 실패 (findByNameIn 이슈)

이미 해결 완료:
- ✅ Repository에 `findByNameInWithCollation` 메서드 추가
- ✅ 입력값 정규화 (trim 처리)
- ✅ ProfileService에서 새로운 메서드 사용

### Qdrant 연결 실패

```bash
# Qdrant 상태 확인
docker exec dodream-qdrant curl http://localhost:6333/health
```

### DB 데이터 확인

```bash
# 마스터 데이터 확인
docker exec dodream mysql -u root -proot dodream -e "SELECT * FROM role;"
docker exec dodream mysql -u root -proot dodream -e "SELECT * FROM tech_skill;"
docker exec dodream mysql -u root -proot dodream -e "SELECT * FROM interest_keyword;"

# 프로필 확인
docker exec dodream mysql -u root -proot dodream -e "SELECT id, nickname FROM profile LIMIT 5;"

# 게시글 확인
docker exec dodream mysql -u root -proot dodream -e "SELECT id, title FROM post LIMIT 5;"
```

## 완료 확인 체크리스트

- [ ] 구글 로그인 성공 및 JWT 토큰 획득
- [ ] 프로필 생성 API 성공 (findByNameIn 정상 작동)
- [ ] 게시글 생성 API 성공
- [ ] 게시글 임베딩이 Qdrant에 저장됨 (확인 가능)
- [ ] 추천 API가 유사도 기반 게시글 반환

## 로그 확인

애플리케이션 로그에서 다음을 확인:

```bash
# 프로필 생성 시
프로필 생성 - 직군: [...], 관심분야: [...], 기술스택: [...]
조회된 직군 개수: X / 요청: X

# 게시글 생성 시
게시글 임베딩 생성 완료
Qdrant 저장 완료

# 추천 API 호출 시
사용자 프로필 임베딩 로드
Qdrant 유사도 검색 완료
```
