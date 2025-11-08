package swyp.dodream.domain.notification.badge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.notification.badge.domain.BadgeStatus;

public interface BadgeStatusRepository extends JpaRepository<BadgeStatus, Long> {

}