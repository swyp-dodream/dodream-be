package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import swyp.dodream.common.entity.BaseEntity;

@Entity
@Table(name = "tech_skill")
@Getter
@Setter
@NoArgsConstructor
public class TechSkill extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TechCategory category;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

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
