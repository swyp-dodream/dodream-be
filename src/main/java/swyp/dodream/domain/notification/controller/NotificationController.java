package swyp.dodream.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import swyp.dodream.domain.notification.domain.Notification;
import swyp.dodream.domain.notification.dto.NotificationResponse;
import swyp.dodream.domain.notification.infra.SseEmitterPool;
import swyp.dodream.domain.notification.repository.NotificationRepository;
import swyp.dodream.domain.notification.service.NotificationService;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.profile.repository.ProfileRepository;
import swyp.dodream.jwt.dto.UserPrincipal;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final SseEmitterPool sseEmitterPool;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final ProfileRepository profileRepository;

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
    public List<NotificationResponse> list(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();

        List<Notification> notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(userId);

        Set<Long> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Integer> profileImageMap = profileRepository.findByUserIdIn(senderIds).stream()
                .collect(Collectors.toMap(Profile::getUserId, Profile::getProfileImageCode));

        return notifications.stream()
                .map(n -> NotificationResponse.of(
                        n,
                        n.getSenderId() != null ? profileImageMap.get(n.getSenderId()) : null
                ))
                .toList();
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = "본인 알림일 경우에만 읽음 처리"
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> read(@PathVariable Long id, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.status(204).build();
    }

    @Operation(
            summary = "[DEV_TEST] 특정 사용자에게 테스트 알림 발송",
            description = "개발/테스트 환경에서 사용"
    )
    @PostMapping("/dev/notify")
    public void notifyTest(@RequestParam Long receiverId) {
        notificationService.sendProposalNotificationToUser(receiverId, null,999L, "테스터", "글제목", null);
    }
}