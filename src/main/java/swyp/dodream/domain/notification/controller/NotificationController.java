package swyp.dodream.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import swyp.dodream.domain.notification.domain.Notification;
import swyp.dodream.domain.notification.infra.SseEmitterPool;
import swyp.dodream.domain.notification.repository.NotificationRepository;
import swyp.dodream.domain.notification.service.NotificationService;
import swyp.dodream.jwt.dto.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseEmitterPool sseEmitterPool;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Operation(
            summary = "알림 SSE 구독",
            description = "로그인한 사용자의 알림 스트림을 구독"
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/stream")
    public SseEmitter stream(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();
        return sseEmitterPool.subscribe(userId);
    }

    @Operation(
            summary = "알림 목록 조회",
            description = "로그인한 사용자의 알림 목록을 최신순으로 조회"
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<Notification> list(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();
        return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(userId);
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = "본인 알림일 경우에만 읽음 처리"
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/read")
    public void read(@PathVariable Long id, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();
        notificationService.markAsRead(id, userId);
    }

    @Operation(
            summary = "[DEV_TEST] 특정 사용자에게 테스트 알림 발송",
            description = "개발/테스트 환경에서 사용"
    )
    @PostMapping("/dev/notify")
    public void notifyTest(@RequestParam Long receiverId) {
        notificationService.sendProposalNotificationToUser(receiverId, 999L, "테스터", "글제목");
    }
}