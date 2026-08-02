package com.onestop.search.web;

import com.onestop.search.service.SearchService;
import com.onestop.search.web.dto.SearchDtos.FacetedSearchResponse;
import com.onestop.search.web.dto.SearchDtos.ReindexResult;
import com.onestop.search.web.dto.SearchDtos.SuggestionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    private static final int MAX_PAGE_SIZE = 100;

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /** GET /api/search/suggest?q=milk — autocomplete suggestions. */
    @GetMapping("/api/search/suggest")
    public List<SuggestionDto> suggest(@RequestParam String q,
                                       @RequestParam(defaultValue = "8") int limit) {
        return searchService.suggest(q, Math.min(Math.max(limit, 1), 20));
    }

    /** GET /api/search?q=&category=&brand=&page=&size= — full search results with facets. */
    @GetMapping("/api/search")
    public FacetedSearchResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return searchService.search(q, category, brand, safePage, safeSize);
    }

    /** POST /internal/search/reindex — rebuild the index from the catalog (not gateway-exposed). */
    @PostMapping("/internal/search/reindex")
    public ReindexResult reindex() {
        return new ReindexResult(searchService.reindex());
    }
}
