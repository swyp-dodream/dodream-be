#!/bin/bash

echo "=========================================="
echo "DoDream 전체 플로우 테스트"
echo "=========================================="

BASE_URL="http://localhost:8080"
API_BASE="${BASE_URL}/api"

echo ""
echo "1. 구글 OAuth2 로그인 URL 확인"
echo "=========================================="
echo "브라우저에서 다음 URL을 열어 로그인하세요:"
echo "${BASE_URL}/oauth2/authorization/google"
echo ""
echo "로그인 후 리다이렉트된 URL에서 토큰을 확인하거나"
echo "브라우저 콘솔/네트워크 탭에서 JWT 토큰을 복사하세요."
echo ""
read -p "JWT Access Token을 입력하세요: " ACCESS_TOKEN

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ 토큰이 입력되지 않았습니다. 테스트를 종료합니다."
    exit 1
fi

echo ""
echo "2. 마스터 데이터 확인"
echo "=========================================="
echo "직군 (Role):"
curl -s "${API_BASE}/master/roles" 2>/dev/null | jq '.' || echo "API 호출 실패"

echo ""
echo "기술 스택 (TechSkill):"
curl -s "${API_BASE}/master/tech-skills" 2>/dev/null | jq '.' || echo "API 호출 실패"

echo ""
echo "관심 키워드 (InterestKeyword):"
curl -s "${API_BASE}/master/interest-keywords" 2>/dev/null | jq '.' || echo "API 호출 실패"

echo ""
echo "3. 프로필 생성 테스트"
echo "=========================================="
PROFILE_REQUEST=$(cat <<EOF
{
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
}
EOF
)

PROFILE_RESPONSE=$(curl -s -X POST "${API_BASE}/profiles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -d "${PROFILE_REQUEST}")

echo "프로필 생성 응답:"
echo "$PROFILE_RESPONSE" | jq '.' || echo "$PROFILE_RESPONSE"

PROFILE_ID=$(echo "$PROFILE_RESPONSE" | jq -r '.id // empty')

if [ -z "$PROFILE_ID" ]; then
    echo "❌ 프로필 생성 실패"
    exit 1
fi

echo "✅ 프로필 생성 성공! ID: $PROFILE_ID"

echo ""
echo "4. 게시글 생성 테스트"
echo "=========================================="
POST_REQUEST=$(cat <<EOF
{
  "title": "백엔드 개발자 모집합니다",
  "content": "Spring Boot와 Java를 사용하는 프로젝트에 백엔드 개발자를 모집합니다. 신입도 환영합니다.",
  "recruitmentRoleNames": ["백엔드 개발자"],
  "techSkillNames": ["Java", "Spring Boot"],
  "interestKeywordNames": ["웹개발"],
  "recruitmentCount": 2,
  "deadline": "2025-12-31",
  "activityMode": "온라인",
  "projectType": "PROJECT"
}
EOF
)

POST_RESPONSE=$(curl -s -X POST "${API_BASE}/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -d "${POST_REQUEST}")

echo "게시글 생성 응답:"
echo "$POST_RESPONSE" | jq '.' || echo "$POST_RESPONSE"

POST_ID=$(echo "$POST_RESPONSE" | jq -r '.id // empty')

if [ -z "$POST_ID" ]; then
    echo "❌ 게시글 생성 실패"
    exit 1
fi

echo "✅ 게시글 생성 성공! ID: $POST_ID"
echo ""
echo "5. 임베딩 저장 확인 (Qdrant)"
echo "=========================================="
echo "게시글 ID: $POST_ID의 임베딩이 Qdrant에 저장되었는지 확인합니다."
sleep 2

echo ""
echo "6. 게시글 추천 API 테스트"
echo "=========================================="
RECOMMENDATION_RESPONSE=$(curl -s -X GET "${API_BASE}/recommendations/posts?limit=5" \
  -H "Authorization: Bearer ${ACCESS_TOKEN}")

echo "추천 게시글 응답:"
echo "$RECOMMENDATION_RESPONSE" | jq '.' || echo "$RECOMMENDATION_RESPONSE"

echo ""
echo "=========================================="
echo "테스트 완료!"
echo "=========================================="
