package swyp.dodream.domain.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatTestController {

    /**
     * 채팅 테스트용 Thymeleaf 뷰를 반환합니다.
     */
    @GetMapping("/chat/test")
    public String chatTestPage() {
        return "chat-test"; // resources/templates/chat-test.html
    }
}
