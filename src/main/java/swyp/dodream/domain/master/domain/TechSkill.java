package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "tech_skill",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"category_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TechSkill {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TechCategory category;

    @Column(nullable = false, length = 50)
    private String name; // React, Spring, Figma 등

    public TechSkill(TechCategory category, String name) {
        this.category = category;
        this.name = name;
    }
}

