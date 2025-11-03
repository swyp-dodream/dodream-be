package swyp.dodream.domain.search.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swyp.dodream.domain.post.dto.PostResponse;
import swyp.dodream.domain.search.service.SearchService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/posts")
    public List<PostResponse> search(@RequestParam String keyword) {
        return searchService.searchPosts(keyword);
    }
}
