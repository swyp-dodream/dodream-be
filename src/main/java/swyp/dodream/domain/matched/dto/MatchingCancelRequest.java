package swyp.dodream.domain.matched.dto;

import swyp.dodream.domain.post.common.CancelReasonCode;

public record MatchingCancelRequest(
        CancelReasonCode reasonCode,
        String reasonText // ERD엔 텍스트 필드 없지만, 사유 상세가 필요할 수 있어 추가
) {}