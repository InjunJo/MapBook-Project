package com.scaling.libraryservice.search.repository;

import static com.scaling.libraryservice.search.entity.QBook.book;
import static com.scaling.libraryservice.search.util.SearchMode.BOOLEAN_MODE;
import static com.scaling.libraryservice.search.util.TitleType.TOKEN_ALL_ETC;
import static com.scaling.libraryservice.search.util.TitleType.TOKEN_COMPLEX;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.scaling.libraryservice.search.dto.BookDto;
import com.scaling.libraryservice.search.entity.Book;
import com.scaling.libraryservice.search.util.SearchMode;
import com.scaling.libraryservice.search.util.TitleQuery;
import com.scaling.libraryservice.search.util.TitleTrimmer;
import com.scaling.libraryservice.search.util.TitleType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository

public class BookRepoQueryDsl implements BookRepository {

    private final JPAQueryFactory factory;

    private final static int LIMIT_CNT = 100;
    private final static double SCORE_OF_MATCH = 0.0;

    @Override
    public Page<BookDto> findBooks(TitleQuery titleQuery, Pageable pageable) {
        // match..against 문을 활용하여 Full text search를 수행

        JPAQuery<Book> books = getFtSearchJPAQuery(titleQuery, pageable);

        // 최종적으로 페이징 처리된 도서 검색 결과를 반환.
        return PageableExecutionUtils.getPage(
            books.fetch().stream().map(BookDto::new).collect(Collectors.toList()),
            pageable, () -> LIMIT_CNT);
    }

    private JPAQuery<Book> getFtSearchJPAQuery(TitleQuery titleQuery,
        Pageable pageable) {

        BooleanBuilder builder = configBuilder(titleQuery);

        return factory
            .selectFrom(book)
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());
    }

    private BooleanBuilder configBuilder(TitleQuery titleQuery){

        BooleanBuilder builder = new BooleanBuilder();
        SearchMode mode = titleQuery.getTitleType().getMode();

        if (titleQuery.getTitleType() == TOKEN_ALL_ETC) {
            addFullTextSearchQuery(builder, mode, titleQuery.getEtcToken(), book.title);
        } else {
            addFullTextSearchQuery(builder, mode, titleQuery.getNnToken(), book.titleToken);
            
            // 명사와 나머지 단어들도 함께 찾아야 하면 title에 대한 Full Text 구문을 추가 한다.
            if (titleQuery.getTitleType() == TOKEN_COMPLEX) {
                addFullTextSearchQuery(builder, titleQuery.getTitleType().getSecondMode(), titleQuery.getEtcToken(), book.title);
            }
        }

        return builder;
    }

    private void addFullTextSearchQuery(BooleanBuilder builder, SearchMode mode,
        String token, StringPath colum) {

        builder.and(
            getTemplate(
                mode,
                token, colum).gt(
                SCORE_OF_MATCH));
    }


//    private long getTotalSizeForPaging(TitleQuery titleQuery) {
//
//        // 1.키워드가 하나인 포괄적 키워드는 count query 성능을 위해 size를 제한 한다.
//        // 2.그럼에도 결과값은 전체 대출 횟수를 기준으로 내림 차순으로 보여주기 때문에 검색 품질은 보장한다.
//
//        if (titleQuery.getTitleType() == TitleType.TOKEN_ONE) {
//            return LIMIT_CNT;
//        } else {
//            //키워드가 2개 이상일 땐, countQuery 메소드를 호출한다.
//            return countQuery(titleQuery, book.titleToken).fetchOne();
//        }
//    }


    private JPAQuery<Long> countQuery(TitleQuery titleQuery, StringPath colum) {

        return factory
            .select(book.count())
            .from(book)
            .where(
                getTemplate(
                    titleQuery.getTitleType().getMode(),
                    titleQuery.getNnToken(),
                    colum).gt(SCORE_OF_MATCH));
    }

    // 사용자가 입력한 제목 쿼리를 분석한 결과를 바탕으로 boolean or natural 모드를 동적으로 선택
    NumberTemplate<Double> getTemplate(SearchMode mode, String name, StringPath colum) {

        String function;

        if (mode == BOOLEAN_MODE) {
            function = "function('BooleanMatch',{0},{1})";

            // boolean 모드에서 모두 반드시 포함된 결과를 위해 '+'를 붙여주는 정적 메소드 호출.
            name = TitleTrimmer.splitAddPlus(name);
        } else {
            function = "function('NaturalMatch',{0},{1})";
        }

        return Expressions.numberTemplate(Double.class,
            function, colum, name);
    }

    private JPAQuery<Long> countQueryAll() {

        return factory
            .select(book.count())
            .from(book);
    }

    // csv file로 변환 할 때 사용하기 위한 메소드.
    public Page<Book> findAllAndSort(Pageable pageable) {

        JPAQuery<Book> books = factory
            .selectFrom(book)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(book.loanCnt.desc());

        return PageableExecutionUtils.getPage(
            books.fetch(),
            pageable, () -> countQueryAll().fetchOne());
    }

}
