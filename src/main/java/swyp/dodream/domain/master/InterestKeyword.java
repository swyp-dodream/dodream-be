package swyp.dodream.domain.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "interest_keyword",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"category_id", "name"})
        }
)
@Getter
@NoArgsConstructor
public class InterestKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private InterestCategory category;

    @Column(nullable = false, length = 50)
    private String name; // AI, 모빌리티, 데이터 등
}
