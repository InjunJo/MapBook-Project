package com.scaling.libraryservice.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;


@Getter
@NoArgsConstructor
@ToString
@Builder
@AllArgsConstructor
public class MetaDto {

    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
    private String searchTime;

    private String userQuery;

    public void addSearchTime(String searchTime) {
        this.searchTime = searchTime;
    }

    public MetaDto(@NonNull Page<BookDto> books, @NonNull ReqBookDto reqBookDto) {

        this.totalPages = books.getTotalPages();
        this.totalElements = books.getTotalElements();
        this.currentPage = reqBookDto.getPage();
        this.pageSize = reqBookDto.getSize();
        this.userQuery = reqBookDto.getQuery();
    }

    public static MetaDto isbnMetaDto(String userQuery) {
        return MetaDto.builder()
            .totalPages(1)
            .totalElements(1)
            .currentPage(1)
            .pageSize(1)
            .userQuery(userQuery)
            .build();
    }

    public static MetaDto sessionMetaDto(String searchTime){
        return MetaDto.builder()
            .totalPages(1)
            .totalElements(1)
            .currentPage(1)
            .pageSize(1)
            .searchTime(searchTime)
            .build();
    }


    public static MetaDto emptyDto(String userQuery) {

        return MetaDto.builder()
            .userQuery(userQuery)
            .totalPages(0)
            .totalElements(0)
            .currentPage(0)
            .pageSize(0)
            .searchTime("")
            .build();
    }

}