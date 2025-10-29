package swyp.dodream.domain.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.interest.domain.Interest;
import swyp.dodream.domain.interest.enums.InterestEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    List<Interest> findByProfileId(Long profileId);

    boolean existsByProfileIdAndInterest(Long profileId, InterestEnum interest);

    long countByProfileId(Long profileId);

    @Modifying
    @Query("DELETE FROM Interest i WHERE i.profile = :profile AND i.interest = :interest")
    void deleteByProfileAndInterest(@Param("profile") Profile profile, @Param("interest") InterestEnum interest);

    void deleteByProfile(Profile profile);
}
