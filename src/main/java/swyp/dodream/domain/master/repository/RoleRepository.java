package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.RoleCode;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(RoleCode code);

    Optional<Role> findByName(String name);

    Set<Role> findByIdIn(Set<Long> ids);

    Set<Role> findByNameIn(Set<String> names);
}
