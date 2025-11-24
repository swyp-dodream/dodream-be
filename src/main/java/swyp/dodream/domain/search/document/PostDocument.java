package swyp.dodream.domain.search.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "posts")
@Setting(settingPath = "elasticsearch/post-settings.json")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDocument {

    @Id
    private Long id;

    /**
     * 제목: post_analyzer 사용
     * - nori_tokenizer로 한글 형태소 분석
     * - lowercase 필터로 대소문자 통일
     * - my_synonyms 필터로 동의어 처리
     */
    @Field(type = FieldType.Text, analyzer = "post_analyzer")
    private String title;

    /**
     * 내용: post_analyzer 사용
     * 제목과 동일한 분석기 적용
     */
    @Field(type = FieldType.Text, analyzer = "post_analyzer")
    private String description;
}