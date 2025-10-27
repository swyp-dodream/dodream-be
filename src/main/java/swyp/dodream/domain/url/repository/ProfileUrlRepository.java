package swyp.dodream.domain.url.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.profile.domain.Profile;
import swyp.dodream.domain.url.domain.ProfileUrl;
import swyp.dodream.domain.url.enums.UrlLabel;

import java.util.List;
import java.util.Optional;

public interface ProfileUrlRepository extends JpaRepository<ProfileUrl, Long> {
    
    List<ProfileUrl> findByProfile(Profile profile);
    
    List<ProfileUrl> findByProfileId(Long profileId);
    
    Optional<ProfileUrl> findByProfileAndLabel(Profile profile, UrlLabel label);
    
    Optional<ProfileUrl> findByProfileIdAndLabel(Long profileId, UrlLabel label);
    
    boolean existsByProfileAndLabel(Profile profile, UrlLabel label);
    
    boolean existsByProfileIdAndLabel(Long profileId, UrlLabel label);
}