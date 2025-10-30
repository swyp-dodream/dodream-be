package swyp.dodream.domain.proposal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.proposal.domain.ProposalNotification;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalNotificationResponse {
    @JsonProperty("proposal_project_on")
    private Boolean proposalProjectOn;

    @JsonProperty("proposal_study_on")
    private Boolean proposalStudyOn;

    public static ProposalNotificationResponse from(ProposalNotification notification) {
        if (notification == null) {
            return null;
        }
        return ProposalNotificationResponse.builder()
                .proposalProjectOn(notification.getProposalProjectOn())
                .proposalStudyOn(notification.getProposalStudyOn())
                .build();
    }
}
