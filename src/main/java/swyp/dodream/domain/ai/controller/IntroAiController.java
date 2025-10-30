package swyp.dodream.domain.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.ai.dto.IntroAiDraftRequest;
import swyp.dodream.domain.ai.service.AiDraftService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class IntroAiController {

    private final AiDraftService aiDraftService;

    @Operation(summary = "AI 자기소개 초안 생성", description = "프로필 데이터 기반으로 200자 이내 자기소개 초안을 생성합니다.")
    @PostMapping("/intro/ai-draft")
    public ResponseEntity<String> generateIntro(Authentication authentication,
                                                @Valid @RequestBody IntroAiDraftRequest request) {
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        String draft = aiDraftService.createIntroDraft(user.getUserId(), request);
        return ResponseEntity.ok(draft);
    }
}