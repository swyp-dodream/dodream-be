package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "interest_keyword",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"category_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class InterestKeyword {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private InterestCategory category;

    @Column(nullable = false, length = 50)
    private String name;
}
