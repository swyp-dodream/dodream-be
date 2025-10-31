package swyp.dodream.domain.ai.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swyp.dodream.domain.ai.dto.IntroAiDraftRequest;

@Service
@RequiredArgsConstructor
public class AiDraftService {

    private final AiService aiService;

    public String createIntroDraft(Long userId, IntroAiDraftRequest req) {
        String systemPrompt = """
            너는 팀원 모집용 자기소개를 작성하는 전문가다.
            이 자기소개의 목적은 스터디나 프로젝트 팀원을 구하는 것이다.
            
            [핵심 원칙]
            1. 180자 이상 필수로 글을 작성할 것
            2. 협업 가능성과 팀 기여도가 자연스럽게 드러나도록 작성
            3. 기술 스택과 관심 분야를 구체적으로 연결
            4. 편안하고 친근한 어조로 작성하되, 존댓말 사용
            5. "같이 하고 싶다"는 느낌이 자연스럽게 전달되도록
            6. AI가 작성한 티가 나지 않도록 자연스러운 문장 구조 사용
            
            [어미 통일 규칙]
            - 모든 문장은 "~습니다", "~니다", "~어요" 중 하나로 통일
            - 한 자기소개 내에서 어미를 섞지 말 것
            - "~습니다" 체를 기본으로 사용하되, 마지막 문장은 "~어요"로 부드럽게 마무리
            - 문장이 중간에 끊기지 않도록 접속사를 자연스럽게 사용
            
            [글자 수 제한 - 매우 중요!]
            - 최소 180자, 최대 195자
            - 200자를 절대 넘지 말 것
            - 마지막 문장이 200자 제한에 의해서 잘리게 된다면 해당 문장은 버려줘
            
            [작성 구조]
            1문장: 닉네임 + 직군 + 핵심 기술 스택을 자연스럽게 소개
            2문장: 관심 분야를 기술 스택과 연결하여 구체적으로 어떤 프로젝트를 하고 싶은지 서술
            3문장: 본인이 기여할 수 있는 부분과 선호하는 협업 방식을 자연스럽게 연결
            
            [필수 포함 요소]
            - 어떤 프로젝트나 스터디를 하고 싶은지 구체적으로
            - 본인이 팀에 기여할 수 있는 부분 (기술적 강점)
            - 선호하는 협업 방식 (온라인/오프라인/혼합)
            - 함께 배우고 성장하려는 의지
            
            [절대 금지 표현]
            - "혁신적인", "효율적인", "최적의", "탁월한" 같은 과장된 형용사
            - "솔루션 개발", "시스템 구축", "가치 창출" 같은 회사 보고서 용어
            - "~에 흥미를 느낍니다", "~을 지향합니다" 같은 격식체
            - "열심히 하겠습니다", "최선을 다하겠습니다" 같은 클리셰
            - 문장 중간에 갑자기 끊기는 표현
            
            [권장 표현]
            - "~를 함께 만들어보고 싶습니다"
            - "~에 관심 있는 분들과 협업하고 싶어요"
            - "~를 공부하고 있고, 프로젝트에 적용해보고 싶습니다"
            - "~는 제가 도움 드릴 수 있습니다"
            - "함께 고민하며 성장하는 과정을 즐깁니다"
            
            [문장 연결 규칙]
            - 각 문장이 자연스럽게 이어지도록 접속사 활용
            - "그리고", "또한", "특히" 같은 연결어를 적절히 사용
            - 갑작스러운 주제 전환 금지
            - 마지막 문장은 자연스럽게 마무리
            
            [톤 앤 매너]
            - 진지하되 친근한 느낌
            - 자신감 있되 겸손한 태도
            - 프로페셔널하되 부담스럽지 않게
            - 동료를 찾는 느낌 (상사나 멘토를 찾는 느낌 X)
            
            [작성 예시]
            좋은 예: "홍길동입니다. 백엔드 개발자로 Spring과 Java를 다루고 있습니다. AI 기반 추천 시스템 프로젝트를 함께 진행하고 싶고, REST API 설계와 데이터베이스 최적화는 제가 기여할 수 있습니다. 오프라인에서 주 1~2회 만나 코드 리뷰하며 함께 성장하는 걸 선호해요."
            
            나쁜 예: "백엔드 엔지니어입니다. 혁신적인 솔루션을 구축하고 싶습니다. 효율적인 시스템. 열심히 하겠습니다."
            """;

        String userPrompt = buildIntroPrompt(req);
        String raw = aiService.generate(systemPrompt, userPrompt);
        return trimTo200(clean(raw));
    }

    private String buildIntroPrompt(IntroAiDraftRequest r) {
        String urls = (r.getProfileUrls() == null || r.getProfileUrls().isEmpty())
                ? "없음"
                : "있음 (포트폴리오 보유)";

        String existingIntro = (r.getIntroText() == null || r.getIntroText().isBlank())
                ? "없음"
                : r.getIntroText();

        String roles = String.join(", ", r.getRoles());
        String interests = String.join(", ", r.getInterestKeywords());
        String skills = String.join(", ", r.getTechSkills());

        return String.format("""
            [사용자 프로필]
            닉네임: %s
            연령대: %s
            경력 수준: %s
            선호 활동 방식: %s
            직군: %s
            관심 분야: %s
            보유 기술 스택: %s
            포트폴리오: %s
            
            [기존 자기소개]
            %s
            
            [작성 지침]
            위 프로필 정보를 바탕으로 팀원 모집용 자기소개를 작성하라.
            
            반드시 다음 내용을 포함해야 한다:
            1. 본인의 닉네임과 직군을 자연스럽게 소개
            2. 보유 기술 스택을 활용하여 관심 분야에서 무엇을 하고 싶은지
            3. 구체적으로 어떤 프로젝트나 스터디를 함께하고 싶은지
            4. 본인이 기여할 수 있는 기술적 강점
            5. 선호하는 협업 방식
            
            기존 자기소개가 있다면 핵심 내용을 살리되, 팀원 모집 목적에 맞게 재구성하라.
            
            중요: 200자를 반드시 채워야 하며, 문장이 자연스럽게 연결되어야 한다.
            어미는 "~습니다" 체로 통일하되 마지막 문장만 "~어요"로 부드럽게 마무리하라.
            """,
                r.getNickname(),
                r.getAgeBand(),
                r.getExperience(),
                r.getActivityMode(),
                roles,
                interests,
                skills,
                urls,
                existingIntro
        );
    }

    private String clean(String s) {
        if (s == null) return "";
        return s.replaceAll("[\"`]+", "").replaceAll("\\s+", " ").trim();
    }

    private String trimTo200(String s) {
        return (s.length() <= 200) ? s : s.substring(0, 200);
    }
}