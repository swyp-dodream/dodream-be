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

    // 프로젝트 제안 수신 여부 토글
    public void toggleProposalProject() {
        this.proposalProjectOn = !this.proposalProjectOn;
    }

    // 스터디 제안 수신 여부 토글
    public void toggleProposalStudy() {
        this.proposalStudyOn = !this.proposalStudyOn;
    }

    // 둘 다 토글
    public void toggleAll() {
        toggleProposalProject();
        toggleProposalStudy();
    }

    // 특정 제안 타입 허용 여부 확인
    public boolean isProposalAllowed(String type) {
        if ("PROJECT".equalsIgnoreCase(type)) {
            return proposalProjectOn;
        } else if ("STUDY".equalsIgnoreCase(type)) {
            return proposalStudyOn;
        }
        return false;
    }
}
