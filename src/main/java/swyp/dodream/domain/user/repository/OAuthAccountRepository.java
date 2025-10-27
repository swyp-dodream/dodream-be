package swyp.dodream.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.user.domain.OAuthAccount;
import swyp.dodream.login.domain.AuthProvider;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    
    Optional<OAuthAccount> findByUserIdAndProvider(Long userId, AuthProvider provider);
    
    Optional<OAuthAccount> findByEmailAndProvider(String email, AuthProvider provider);
    
    boolean existsByEmailAndProvider(String email, AuthProvider provider);
    
    Optional<OAuthAccount> findByUserId(Long userId);
}
