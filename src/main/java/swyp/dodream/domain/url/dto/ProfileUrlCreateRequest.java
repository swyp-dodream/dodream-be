package swyp.dodream.domain.url.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import swyp.dodream.domain.url.enums.UrlLabel;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUrlCreateRequest {
    private UrlLabel label;
    private String url;
}