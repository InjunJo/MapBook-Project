package com.scaling.libraryservice.mapBook.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.scaling.libraryservice.commons.api.service.LoanableLibConnGenerator;
import com.scaling.libraryservice.mapBook.dto.LibraryInfoDto;
import com.scaling.libraryservice.mapBook.dto.ReqMapBookDto;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanableLibConnGeneratorTest {

    @InjectMocks
    private LoanableLibConnGenerator generator;

    String isbn13 = "1234567890";
    int areaCd = 26000;

    private ReqMapBookDto reqMapBookDto;
    @BeforeEach
    public void setUp(){
        reqMapBookDto = ReqMapBookDto.builder().areaCd(areaCd).isbn(isbn13).build();
    }

    @Test @DisplayName("소장 도서 전처리를 지원하는 지역을 대상으로 필요한 API Connection 객체를 선별해서 만들 수 있다")
    void generateNecessaryConns_when_Support_Area() {
        /* given */

        LibraryInfoDto library1 = LibraryInfoDto.builder().hasBook(true).libNo(1).isHasBookSupport(true).build();
        LibraryInfoDto library2 = LibraryInfoDto.builder().hasBook(false).libNo(2).isHasBookSupport(true).build();

        List<LibraryInfoDto> libraries = Arrays.asList(library1, library2);



        /* when */

        var result= generator.generateNecessaryConns(libraries,reqMapBookDto);

        /* then */

        assertEquals(1,result.size());
    }

    @Test @DisplayName("소장 도서 전처리를 지원하지 않는 지역은 도서관 별로 API Connection을 모두 만든다")
    void generateNecessaryConns_when_Area_IsNotSupported() {
        /* given */

        LibraryInfoDto library1 = LibraryInfoDto.builder().hasBook(false).isHasBookSupport(false).libNo(1).build();

        LibraryInfoDto library2 = LibraryInfoDto.builder().hasBook(false).isHasBookSupport(false).libNo(2).build();

        List<LibraryInfoDto> libraries = Arrays.asList(library1, library2);

        /* when */

        var result= generator.generateNecessaryConns(libraries,reqMapBookDto);

        /* then */

        assertEquals(2,result.size());
    }

    @Test @DisplayName("관련 도서관이 없으면 API Connetion을 만들지 않는다")
    void generateNecessaryConns_empty_library() {
        /* given */

        List<LibraryInfoDto> libraries = Collections.emptyList();

        /* when */

        var result= generator.generateNecessaryConns(libraries,reqMapBookDto);

        /* then */

        assertEquals(0,result.size());
    }

}