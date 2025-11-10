package swyp.dodream.domain.profile.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import swyp.dodream.domain.profile.domain.Profile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("select p from Profile p where p.userId = :userId")
    Optional<Profile> findByUserId(@Param("userId") Long userId);
    
    boolean existsByNickname(String nickname);
    
    boolean existsByUserId(Long userId);

    // 마이페이지 조회
    @Query("""
        select distinct p from Profile p
          left join fetch p.roles r
          left join fetch p.interestKeywords ik
          left join fetch p.techSkills ts
          left join fetch p.profileUrls pu
        where p.userId = :userId
        """)
    Optional<Profile> findWithAllByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"roles"})
    List<Profile> findByUserIdIn(Collection<Long> userIds);
}
