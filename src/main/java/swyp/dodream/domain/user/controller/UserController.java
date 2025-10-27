package swyp.dodream.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.user.service.UserService;
import swyp.dodream.jwt.dto.UserPrincipal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "회원탈퇴", description = "사용자 계정을 비활성화합니다 (소프트 딜리션)")
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdrawUser(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getUserId();
        
        userService.withdrawUser(userId);
        
        return ResponseEntity.ok("회원탈퇴가 완료되었습니다.");
    }
}