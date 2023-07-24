package com.scaling.libraryservice.search.controller;

import com.scaling.libraryservice.commons.timer.Timer;
import com.scaling.libraryservice.mapBook.dto.TestingBookDto;
import com.scaling.libraryservice.search.dto.BookDto;
import com.scaling.libraryservice.search.dto.ReqBookDto;
import com.scaling.libraryservice.search.dto.RespBooksDto;
import com.scaling.libraryservice.search.service.BookSearchService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SearchController {

    private final BookSearchService bookSearchService;

    /**
     * 메인 홈페이지를 반환하는 메서드입니다.
     *
     * @return 홈페이지의 View 이름
     */
    @GetMapping("/")
    public String home() {
        return "search/searchResult";
    }

    @Timer
    @PostMapping(value = "/books/autocomplete")
    public ResponseEntity<List<BookDto>> autocomplete(@RequestParam(value = "query") String query) {

        RespBooksDto result = bookSearchService.searchBooks(
            new ReqBookDto(query, 1, 5), 3);

        return ResponseEntity.ok(result.getDocuments());
    }

    @GetMapping("/books/search")
    @Timer
    public ResponseEntity<RespBooksDto> searchBook(@RequestParam(value = "query", required = false) String query,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {

        RespBooksDto searchResult
            = bookSearchService.searchBooks(new ReqBookDto(query, page, size), 3);

        return ResponseEntity.ok(searchResult);
    }

    @GetMapping("/books/search/test")
    public ResponseEntity<RespBooksDto> restSearchBook(@NonNull @RequestBody TestingBookDto testingBookDto) {

        RespBooksDto result = bookSearchService.searchBooks(
            new ReqBookDto(testingBookDto.getBookName(), 1,10),3);

        if (result.getDocuments().isEmpty()) {
            log.info("[Not Found]This book is Not Found");
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

}