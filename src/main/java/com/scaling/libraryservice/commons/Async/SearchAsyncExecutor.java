package com.scaling.libraryservice.commons.Async;

import com.scaling.libraryservice.commons.caching.CustomCacheManager;
import com.scaling.libraryservice.search.cacheKey.ReqBookDto;
import com.scaling.libraryservice.search.dto.BookDto;
import com.scaling.libraryservice.search.dto.MetaDto;
import com.scaling.libraryservice.search.dto.RespBooksDto;
import com.scaling.libraryservice.search.service.BookSearchService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j @Component
public class SearchAsyncExecutor implements AsyncExecutor<Page<BookDto>,ReqBookDto>{

    private final CustomCacheManager<ReqBookDto,RespBooksDto> cacheManager;

    @Override
    public Page<BookDto> execute(Supplier<Page<BookDto>> supplier,ReqBookDto reqBookDto,int timeout) {

        Page<BookDto> booksPage = Page.empty();

        try{
            booksPage = CompletableFuture.supplyAsync(supplier)
                .get(timeout, TimeUnit.SECONDS);

        }catch (TimeoutException | InterruptedException | ExecutionException e) {
            log.error("Query execution exceeded 3 seconds. Returning an empty result.");
            asyncSearchBook(supplier,reqBookDto);
        }

        return booksPage;
    }

    private void asyncSearchBook(Supplier<Page<BookDto>> supplier, ReqBookDto reqBookDto) {

        log.info("[{}] async Search Book start.....", reqBookDto.getQuery());
        CompletableFuture.runAsync(() -> {
            Page<BookDto> fetchedBooks = supplier.get();

            RespBooksDto item = constructRespBooksDto(fetchedBooks,reqBookDto);

            cacheManager.put(BookSearchService.class,reqBookDto,item);

            log.info("[{}] async Search task is Completed", reqBookDto.getQuery());
        });
    }


    private RespBooksDto constructRespBooksDto(Page<BookDto> fetchedBooks, ReqBookDto reqBookDto){
        return new RespBooksDto(
            new MetaDto(fetchedBooks, reqBookDto), fetchedBooks);
    }
}
