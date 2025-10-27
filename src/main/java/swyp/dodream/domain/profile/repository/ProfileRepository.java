package swyp.dodream.domain.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.profile.domain.Profile;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    
    Optional<Profile> findByUserId(Long userId);
    
    boolean existsByNickname(String nickname);
    
    boolean existsByUserId(Long userId);
}
