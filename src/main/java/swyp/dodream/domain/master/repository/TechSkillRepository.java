package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.master.domain.TechSkill;

import java.util.Optional;
import java.util.Set;

public interface TechSkillRepository extends JpaRepository<TechSkill, Long> {
    Optional<TechSkill> findByName(String name);

    Set<TechSkill> findByNameIn(Set<String> names);

    Set<TechSkill> findByIdIn(Set<Long> ids);
}
