package swyp.dodream.login.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "인증", description = "OAuth2 로그인 관련 API")
@RestController
@RequestMapping("/api")
public class AuthController {

    @Operation(summary = "로그인 성공", description = "OAuth2 로그인 성공 후 사용자 정보를 반환합니다.")
    @GetMapping("/login/success")
    public ResponseEntity<Map<String, Object>> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();
        response.put("email", principal.getAttribute("email"));
        response.put("name", principal.getAttribute("name"));
        response.put("picture", principal.getAttribute("picture"));
        response.put("message", "로그인 성공!");
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "사용자 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "인증되지 않은 사용자입니다."));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("email", principal.getAttribute("email"));
        response.put("name", principal.getAttribute("name"));
        response.put("picture", principal.getAttribute("picture"));
        
        return ResponseEntity.ok(response);
    }
}

