package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.master.domain.TechSkill;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.master.domain.TechSkill;
import java.util.*;

public interface TechSkillRepository extends JpaRepository<TechSkill, Long> {
    List<TechSkill> findByNameIn(java.util.Collection<String> names);
}
