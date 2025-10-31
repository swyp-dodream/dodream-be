package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import swyp.dodream.common.entity.BaseEntity;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseEntity {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleCode code;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    public Role(Long id, String name, RoleCode code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public Role(String name, RoleCode code) {
        this.name = name;
        this.code = code;
    }
}