package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.master.domain.InterestKeyword;
import java.util.*;

public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, Long> {
    List<InterestKeyword> findByNameIn(java.util.Collection<String> names);
}
