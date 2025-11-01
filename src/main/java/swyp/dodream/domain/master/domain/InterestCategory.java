package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;

@Entity
@Table(name = "interest_category")
@Getter
@NoArgsConstructor
public class InterestCategory extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public InterestCategory(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public InterestCategory(String name) {
        this.name = name;
    }
}