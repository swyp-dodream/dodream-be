package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;

@Entity
@Table(name = "tech_skills")
@Getter
@NoArgsConstructor
public class TechSkill extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TechCategory category;

    public TechSkill(Long id, String name, TechCategory category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public TechSkill(String name, TechCategory category) {
        this.name = name;
        this.category = category;
    }
}
