package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.RoleCode;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByNameIn(java.util.Collection<String> names);

    Set<Role> findByCodeIn(Set<RoleCode> codes);
}

