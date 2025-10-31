package swyp.dodream.domain.feedback.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackOption {

    // ===================================
    // 긍정 표현
    // ===================================
    GOOD_COMMUNICATION("소통이 원활해요"),
    KEEPS_PROMISES("일정 약속을 잘 지켜요"),
    RESPONSIBLE("맡은 역할을 책임감 있게 수행해요"),
    POSITIVE_ENERGY("긍정적인 에너지가 좋아요"),
    PROBLEM_SOLVER("문제 해결력이 뛰어나요"),
    RESPECTS_OPINIONS("다른 사람의 의견을 존중해요"),

    // ===================================
    // 부정 표현
    // ===================================
    POOR_COMMUNICATION("소통이 원활하지 않아요"),
    IGNORES_OPINIONS("다른 사람의 의견을 잘 듣지 않아요"),
    LACKS_RESPONSIBILITY("맡은 역할에 대한 책임감이 아쉬워요"),
    NEGATIVE_INFLUENCE("팀 분위기에 부정적인 영향을 줘요"),
    POOR_PROBLEM_SOLVING("문제 상황에서 해결이 다소 미흡했어요"),
    BREAKS_PROMISES("일정을 지키지 않아요");

    private final String description;

}