package swyp.dodream.domain.policy.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import swyp.dodream.domain.policy.dto.PolicyResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class PolicyService {

    public PolicyResponse getPolicy(String type) throws IOException {
        String fileName = switch (type.toUpperCase()) {
            case "TERMS" -> "terms.md";
            case "PRIVACY" -> "privacy.md";
            default -> throw new IllegalArgumentException("잘못된 정책 타입입니다: " + type);
        };

        // classpath:resources/policies/terms.md
        ClassPathResource resource = new ClassPathResource("policies/" + fileName);
        String content = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);

        String title = type.equalsIgnoreCase("TERMS") ? "이용약관" : "개인정보처리방침";
        return new PolicyResponse(title, content);
    }
}