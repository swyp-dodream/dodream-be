package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.master.domain.InterestKeyword;

import java.util.Optional;
import java.util.Set;

public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, Long> {
    Optional<InterestKeyword> findByName(String name);

    Set<InterestKeyword> findByNameIn(Set<String> names);

    Set<InterestKeyword> findByIdIn(Set<Long> ids);
}
