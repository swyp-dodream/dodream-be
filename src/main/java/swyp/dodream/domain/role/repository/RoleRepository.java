package swyp.dodream.domain.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.role.domain.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByProfile(Profile profile);
}
