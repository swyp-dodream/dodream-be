package swyp.dodream.domain.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "posts")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDocument {

    @Id
    private Long id;

    private String title;
    private String description;
}
