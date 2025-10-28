package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.master.domain.TechCategory;

public interface TechCategoryRepository extends JpaRepository<TechCategory, Long> {
}