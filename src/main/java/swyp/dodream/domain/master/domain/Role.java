package swyp.dodream.domain.master.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;

@Entity
@Table(name = "role")
@Getter
@NoArgsConstructor
public class Role extends BaseEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleCode code;

    public Role(Long id, String name, RoleCode code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public Role(String name, RoleCode code) {
        this.name = name;
        this.code = code;
    }

    public RoleCode getCode() {
        return code;
    }
}
