package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interest_category")
@Getter
@NoArgsConstructor
public class InterestCategory {

    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 기술, 비즈니스, 사회 등
}
