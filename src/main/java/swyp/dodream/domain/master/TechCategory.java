package swyp.dodream.domain.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tech_category")
@Getter
@NoArgsConstructor
public class TechCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 프론트엔드, 백엔드, 모바일, 디자인
}
