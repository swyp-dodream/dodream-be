package swyp.dodream.domain.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import swyp.dodream.domain.master.domain.TechCategory;

@Getter
@AllArgsConstructor
public class TechCategoryResponse {
    private Long id;
    private String name;

    public static TechCategoryResponse from(TechCategory category) {
        return new TechCategoryResponse(category.getId(), category.getName());
    }
}