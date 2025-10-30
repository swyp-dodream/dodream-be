package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import swyp.dodream.common.entity.BaseEntity;

@Entity
@Table(name = "interest_keyword")
@Getter
@Setter
@NoArgsConstructor
public class InterestKeyword extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private InterestCategory category;

    public InterestKeyword(Long id, String name, InterestCategory category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public InterestKeyword(String name, InterestCategory category) {
        this.name = name;
        this.category = category;
    }
}
