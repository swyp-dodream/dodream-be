package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.master.domain.Role;
import swyp.dodream.domain.master.domain.RoleCode;

import java.util.List;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByNameIn(java.util.Collection<String> names);

    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    List<Role> findByNameInJpql(@Param("names") java.util.Collection<String> names);

    Set<Role> findByCodeIn(Set<RoleCode> codes);
}

