package swyp.dodream.domain.techstack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.techstack.domain.TechStack;
import swyp.dodream.domain.techstack.enums.TechStackEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechStackRepository extends JpaRepository<TechStack, Long> {

    List<TechStack> findByProfileId(Long profileId);

    boolean existsByProfileIdAndTechStack(Long profileId, TechStackEnum techStack);

    @Query("DELETE FROM TechStack t WHERE t.profile = :profile AND t.techStack = :techStack")
    void deleteByProfileAndTechStack(@Param("profile") Profile profile, @Param("techStack") TechStackEnum techStack);
}
