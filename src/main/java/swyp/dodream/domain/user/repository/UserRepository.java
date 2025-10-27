package swyp.dodream.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // 활성 사용자만 조회
    Optional<User> findByIdAndStatusTrue(Long id);
    
    boolean existsByIdAndStatusTrue(Long id);
}
