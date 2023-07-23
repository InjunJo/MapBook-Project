package com.scaling.libraryservice.search.controller;

import com.scaling.libraryservice.commons.timer.Timer;
import com.scaling.libraryservice.search.dto.ReqBookDto;
import com.scaling.libraryservice.search.dto.RespBooksDto;
import com.scaling.libraryservice.search.service.BookSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 도서 검색 기능을 제공하는 웹 컨트롤러입니다. 사용자가 도서 검색 페이지에서 입력한 검색어를 이용하여 검색 결과를 반환합니다.
 */
@RequiredArgsConstructor
@Controller
@Slf4j
public class SearchViewController {

    private final BookSearchService searchService;

    /**
     * 메인 홈페이지를 반환하는 메서드입니다.
     *
     * @return 홈페이지의 View 이름
     */
    @GetMapping("/")
    public String home() {
        return "search/home";
    }


    /**
     * 사용자가 입력한 검색어를 이용해 도서를 검색하고 결과를 반환하는 메서드입니다.
     *
     * @param query  검색어
     * @param page   검색 결과 페이지 번호
     * @param size   페이지 당 반환할 도서 수
     * @param model  검색 결과를 저장할 ModelMap 객체
     * @return 검색 결과 페이지의 View 이름
     */
    @GetMapping("/books/search")
    @Timer
    public String searchBook(@RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        ModelMap model) {

        RespBooksDto searchResult = searchService.searchBooks(new ReqBookDto(query, page, size), 3);

        model.put("searchResult", searchResult);
        model.put("totalPages", searchResult.getMeta().getTotalPages());
        model.put("size", searchResult.getMeta().getTotalElements());
        model.put("searchTime",searchResult.getMeta().getSearchTime());

        return "search/searchResult";
    }

}
