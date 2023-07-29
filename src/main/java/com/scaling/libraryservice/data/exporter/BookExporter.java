package com.scaling.libraryservice.data.exporter;

import com.scaling.libraryservice.data.CsvWriter;
import com.scaling.libraryservice.data.vo.BookVo;
import com.scaling.libraryservice.search.entity.Book;
import com.scaling.libraryservice.search.repository.BookRepoQueryDsl;
import com.scaling.libraryservice.search.engine.TitleAnalyzer;
import com.scaling.libraryservice.search.engine.TitleQuery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class BookExporter extends ExporterService<BookVo, Book> {

    private final TitleAnalyzer titleAnalyzer;
    private final BookRepoQueryDsl bookRepository;

    private Page<Book> page;

    public BookExporter(CsvWriter<BookVo> csvWriter,
        TitleAnalyzer titleAnalyzer, BookRepoQueryDsl bookRepository) {

        super(csvWriter);
        this.titleAnalyzer = titleAnalyzer;
        this.bookRepository = bookRepository;
        this.page = Page.empty();
    }

    @Override
    List<BookVo> analyzeAndExportVo(Pageable pageable, String outputName) {
        // 대출 횟수(loan_cnt) 기준으로 내림 차순으로 데이터를 입력하고,
        // 실제 작업을 수행하기 위해 DB에서 Java단으로 데이터를 가져온다
        this.page = bookRepository.findAllAndSort(pageable);
        List<BookVo> books = new ArrayList<>();

        page.stream().forEach(book -> {
            TitleQuery query = titleAnalyzer.analyze(book.getTitle(), false);
            books.add(new BookVo(book, String.join(" ", query.getNnToken())));
        });

        pageable.next();  // 다음 페이지를 가져오기 위한 설정

        return books;
    }

    public Page<Book> renewPage() {
        return page;
    }

}
