package com.scaling.libraryservice.search.dto;

import com.scaling.libraryservice.commons.timer.TimeMeasurable;
import com.scaling.libraryservice.search.engine.util.SubTitleRemover;
import com.scaling.libraryservice.search.entity.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;

@Getter
@Setter @ToString
@Builder @AllArgsConstructor
public class BookDto {

    private final Long id;

    private final String title;

    private final String content;

    private final String author;

    private final String isbn;

    private String bookImg;

    private Integer loanCnt;


    public BookDto(@NonNull Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.content =  book.getContent().isEmpty() ? "[내용 없음]" : book.getContent();
        this.author = book.getAuthor();
        this.isbn = book.getIsbn();
        this.bookImg = book.getBookImg().isEmpty() ?
            "https://someone-be-bucket.s3.ap-northeast-2.amazonaws.com/mapbook+logo.png"
            : book.getBookImg();
        this.loanCnt = book.getLoanCnt();
    }

    public static BookDto emptyDto(){
        return new BookDto(-1L,"","","","","",0);
    }

    public boolean isEmpty(){
        return this.title.isEmpty() && this.id == -1;
    }
}
