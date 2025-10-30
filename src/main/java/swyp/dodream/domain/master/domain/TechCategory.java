package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;

@Entity
@Table(name = "tech_categories")
@Getter
@NoArgsConstructor
public class TechCategory extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public TechCategory(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TechCategory(String name) {
        this.name = name;
    }
}
