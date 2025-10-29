package swyp.dodream.domain.master.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    // ==============================
    // 기술 카테고리 조회
    // ==============================
    @Operation(
            summary = "기술 카테고리 목록 조회",
            description = "프론트엔드, 백엔드, 모바일, 기타 등 기술 카테고리 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TechCategoryResponse.class)))
    })
    @GetMapping("/categories")
    public ResponseEntity<List<TechCategoryResponse>> getAllCategories() {
        List<TechCategoryResponse> result = techSkillService.getAllCategories();
        return ResponseEntity.ok(result);
    }

    // ==============================
    // 기술 스택 목록 조회
    // ==============================
    @Operation(
            summary = "기술 스택 목록 조회",
            description = "특정 카테고리 ID를 기준으로 기술 스택 목록을 조회합니다. categoryId가 없으면 전체 기술 스택을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TechSkillResponse.class))),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<List<TechSkillResponse>> getSkillsByCategory(
            @RequestParam(required = false) Long categoryId) {
        List<TechSkillResponse> result = techSkillService.getSkills(categoryId);
        return ResponseEntity.ok(result);
    }
}
