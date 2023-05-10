package com.scaling.libraryservice.search.service;

import com.scaling.libraryservice.commons.caching.CustomCacheManager;
import com.scaling.libraryservice.commons.caching.CustomCacheable;
import com.scaling.libraryservice.commons.timer.Timer;
import com.scaling.libraryservice.search.cacheKey.BookCacheKey;
import com.scaling.libraryservice.search.domain.TitleQuery;
import com.scaling.libraryservice.search.domain.TitleType;
import com.scaling.libraryservice.search.dto.BookDto;
import com.scaling.libraryservice.search.dto.MetaDto;
import com.scaling.libraryservice.search.dto.RespBooksDto;
import com.scaling.libraryservice.search.entity.Book;
import com.scaling.libraryservice.search.repository.BookRepository;
import com.scaling.libraryservice.search.util.TitleAnalyzer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 도서 검색 기능을 제공하는 서비스 클래스입니다. 입력된 검색어에 따라 적절한 검색 쿼리를 선택하여 도서를 검색하고, 결과를 반환합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Timer
@Getter
public class BookSearchService {

    private final BookRepository bookRepository;
    private final CustomCacheManager<RespBooksDto> cacheManager;
    private final TitleAnalyzer titleAnalyzer;

    /**
     * 입력된 검색어를 이용하여 도서를 검색하고, 결과를 반환하는 메서드입니다.
     *
     * @param query  검색어
     * @param page   검색 결과 페이지 번호
     * @param size   페이지 당 반환할 도서 수
     * @param target 검색 대상 (기본값: "title")
     * @return 검색 결과를 담은 RespBooksDto 객체
     */
    @Timer
    @CustomCacheable(cacheName = "bookCache")
    public RespBooksDto searchBooks(String query, int page, int size, String target) {

        log.info("-------------query : [{}]-------------------------------", query);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Book> books = null;

        try {
            books = CompletableFuture.supplyAsync(() -> pickSelectQuery(query, pageable))
                .get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Query execution exceeded 3 seconds. Returning an empty result.", e);
            books = Page.empty(pageable);
            asyncSearchBook(query, page, size);
        }

        Objects.requireNonNull(books);

        return new RespBooksDto(
            new MetaDto(books.getTotalPages(),
                books.getTotalElements(), page, size),
            books.stream().map(BookDto::new).toList());
    }

    public void asyncSearchBook(String query, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        log.info("[{}] async Search Book start.....", query);

        var result = CompletableFuture.runAsync(() -> {
            Page<Book> fetchedBooks = pickSelectQuery(query, pageable);

            if (fetchedBooks != null && !fetchedBooks.isEmpty()) {
                RespBooksDto respBooksDto = new RespBooksDto(
                    new MetaDto(fetchedBooks.getTotalPages(),
                        fetchedBooks.getTotalElements(), page, size),
                    fetchedBooks.stream().map(BookDto::new).toList());

                cacheManager.put(this.getClass(), new BookCacheKey(query, page), respBooksDto);
                log.info("[{}] async Search task is Completed", query);
            }
        });
    }


    /**
     * 검색 대상(target)에 따라 적절한 검색 쿼리를 선택하여 도서를 검색하고, 결과를 반환하는 메서드입니다.
     *
     * @param query    검색어
     * @param pageable 페이지 관련 설정을 담은 Pageable 객체
     * @return 검색 결과를 담은 Page<Book> 객체
     */
    public Page<Book> pickSelectQuery(String query, Pageable pageable) {

        TitleQuery titleQuery = titleAnalyzer.analyze(query);

        TitleType type = titleQuery.getTitleType();

        log.info("Query is [{}] and tokens : [{}]", type.name(), titleQuery);

        switch (type) {

            case KOR_SG, KOR_MT_OVER_TWO -> {
                return bookRepository.findBooksByKorNatural(titleQuery.getKorToken(), pageable);
            }
            case ENG_SG -> {
                return bookRepository.findBooksByEngBool(titleQuery.getEngToken(), pageable);
            }
            case KOR_MT_UNDER_TWO -> {
                return bookRepository.findBooksByKorMtFlexible(titleQuery.getKorToken(), pageable);
            }

            case ENG_MT -> {
                return bookRepository.findBooksByEngMtFlexible(titleQuery.getEngToken(), pageable);
            }

            case KOR_ENG, ENG_KOR_SG -> {
                return bookRepository.findBooksByEngKorBool(titleQuery.getEngToken(),
                    titleQuery.getKorToken(), pageable);
            }

            case ENG_KOR_MT -> {
                return bookRepository.findBooksByEngKorNatural(
                    titleQuery.getEngToken(),
                    titleQuery.getKorToken(),
                    pageable);
            }
        }

        return null;
    }


    /**
     * 검색 대상(target)에 따라 적절한 검색 쿼리를 선택하여 도서를 검색하고, 결과를 반환하는 메서드입니다.
     *
     * @param query    검색어
     * @param pageable 페이지 관련 설정을 담은 Pageable 객체
     * @param target   검색 대상 (기본값: "title")
     * @return 검색 결과를 담은 Page<Book> 객체
     */
    private Page<Book> findBooksByTarget(String query, Pageable pageable, String target) {
        if (target.equals("author")) {
            return bookRepository.findBooksByAuthor(query, pageable);
        } else if (target.equals("title")) {
            return bookRepository.findBooksByKorMtFlexible(query, pageable);
        } else {
            return null; //api 추가될 것 고려하여 일단 Null로 넣어놓음
        }
    }

}





