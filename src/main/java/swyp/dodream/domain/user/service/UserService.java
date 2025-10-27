package swyp.dodream.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import swyp.dodream.common.exception.CustomException;
import swyp.dodream.common.exception.ExceptionType;
import swyp.dodream.domain.user.domain.User;
import swyp.dodream.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * 회원탈퇴 (소프트 딜리션)
     */
    @Transactional
    public void withdrawUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));
        
        if (!user.getStatus()) {
            throw new CustomException(ExceptionType.USER_ALREADY_WITHDRAWN);
        }
        
        user.withdraw();
        userRepository.save(user);
    }
    
    /**
     * 활성 사용자 조회
     */
    public User findActiveUserById(Long userId) {
        return userRepository.findById(userId)
                .filter(User::getStatus)
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));
    }
}