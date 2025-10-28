package swyp.dodream.domain.master.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import swyp.dodream.domain.master.dto.TechCategoryResponse;
import swyp.dodream.domain.master.dto.TechSkillResponse;
import swyp.dodream.domain.master.service.TechSkillService;

import java.util.List;

@RestController
@RequestMapping("/api/tech-skills")
@RequiredArgsConstructor
public class TechSkillController {

    private final TechSkillService techSkillService;

    // 1. 기술 카테고리 조회 (프론트엔드 / 백엔드 / 모바일 / 기타)
    @GetMapping("/categories")
    public ResponseEntity<List<TechCategoryResponse>> getAllCategories() {
        List<TechCategoryResponse> result = techSkillService.getAllCategories();
        return ResponseEntity.ok(result);
    }

    // 2. 기술 스택 목록 조회 (카테고리별 필터링 가능)
    @GetMapping
    public ResponseEntity<List<TechSkillResponse>> getSkillsByCategory(
            @RequestParam(required = false) Long categoryId) {
        List<TechSkillResponse> result = techSkillService.getSkills(categoryId);
        return ResponseEntity.ok(result);
    }
}
