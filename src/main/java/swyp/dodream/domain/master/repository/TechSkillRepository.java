package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.master.domain.TechCategory;
import swyp.dodream.domain.master.domain.TechSkill;
import java.util.*;

public interface TechSkillRepository extends JpaRepository<TechSkill, Long> {
    List<TechSkill> findByNameIn(Collection<String> names);

    @Query("SELECT ts FROM TechSkill ts WHERE ts.name IN :names")
    List<TechSkill> findByNameInJpql(@Param("names") Collection<String> names);

    List<TechSkill> findByCategory(TechCategory category);
}