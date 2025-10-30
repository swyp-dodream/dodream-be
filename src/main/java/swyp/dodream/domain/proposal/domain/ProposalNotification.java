package swyp.dodream.domain.proposal.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.common.entity.BaseEntity;

@Entity
@Table(name = "proposal_notifications")
@Getter
@NoArgsConstructor
@Builder
public class ProposalNotification extends BaseEntity {
    @Id
    private Long id;

    @Column(name = "profile_id", nullable = false, unique = true)
    private Long profileId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean proposalProjectOn = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean proposalStudyOn = true;

    // Snowflake ID와 profileId를 받는 생성자
    public ProposalNotification(Long id, Long profileId, Boolean proposalProjectOn, Boolean proposalStudyOn) {
        this.id = id;
        this.profileId = profileId;
        this.proposalProjectOn = proposalProjectOn;
        this.proposalStudyOn = proposalStudyOn;
    }

    public void setProposalProjectOn(Boolean proposalProjectOn) {
        this.proposalProjectOn = proposalProjectOn;
    }

    public void setProposalStudyOn(Boolean proposalStudyOn) {
        this.proposalStudyOn = proposalStudyOn;
    }
}
