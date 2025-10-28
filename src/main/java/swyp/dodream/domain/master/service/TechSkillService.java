package swyp.dodream.domain.master.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import swyp.dodream.domain.master.TechCategory;
import swyp.dodream.domain.master.TechSkill;
import swyp.dodream.domain.master.dto.TechCategoryResponse;
import swyp.dodream.domain.master.dto.TechSkillResponse;
import swyp.dodream.domain.master.repository.TechCategoryRepository;
import swyp.dodream.domain.master.repository.TechSkillRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechSkillService {
    private final TechSkillRepository techSkillRepository;
    private final TechCategoryRepository techCategoryRepository;

    // 카테고리 전체 조회
    public List<TechCategoryResponse> getAllCategories() {
        return techCategoryRepository.findAll()
                .stream()
                .map(TechCategoryResponse::from)
                .collect(Collectors.toList());
    }

    // 카테고리별 스킬 조회
    public List<TechSkillResponse> getSkills(Long categoryId) {
        List<TechSkill> skills;

        if (categoryId == null) {
            skills = techSkillRepository.findAll();
        } else {
            TechCategory category = techCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
            skills = techSkillRepository.findByCategory(category);
        }

        return skills.stream()
                .map(TechSkillResponse::from)
                .collect(Collectors.toList());
    }
}
