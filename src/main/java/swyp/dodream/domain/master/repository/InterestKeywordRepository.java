package swyp.dodream.domain.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.master.domain.InterestKeyword;
import java.util.*;

public interface InterestKeywordRepository extends JpaRepository<InterestKeyword, Long> {
    List<InterestKeyword> findByNameIn(java.util.Collection<String> names);

    @Query("SELECT ik FROM InterestKeyword ik WHERE ik.name IN :names")
    List<InterestKeyword> findByNameInJpql(@Param("names") java.util.Collection<String> names);
}
