package swyp.dodream.domain.proposal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import swyp.dodream.domain.proposal.domain.ProposalNotification;

import java.util.Optional;

public interface ProposalNotificationRepository extends JpaRepository<ProposalNotification, Long> {
    Optional<ProposalNotification> findByProfileId(Long profileId);
}
