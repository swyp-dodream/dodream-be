package swyp.dodream.domain.post.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostFieldId implements Serializable {
    private Long post;
    private Long interestKeyword;
}
