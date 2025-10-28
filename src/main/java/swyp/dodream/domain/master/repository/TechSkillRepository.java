package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.master.domain.TechCategory;
import swyp.dodream.domain.master.domain.TechSkill;

import java.util.List;

public interface TechSkillRepository extends JpaRepository<TechSkill, Long> {
    List<TechSkill> findByCategory(TechCategory category);
}