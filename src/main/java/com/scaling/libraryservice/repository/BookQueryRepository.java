package com.scaling.libraryservice.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.scaling.libraryservice.entity.Book;
import com.scaling.libraryservice.entity.QBook;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookQueryRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    // 검색 + 페이징
    public Page<Book> findBooksByToken(List<String> tokens, Pageable pageable) {
        JPAQuery<Book> query = createBookQueryByToken(tokens);

        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        List<Book> books = query.fetch();
        long totalCount = query.fetchCount();

        return new PageImpl<>(books, pageable, totalCount);
    }

    // 검색용 쿼리 생성
    public JPAQuery<Book> createBookQueryByToken(List<String> tokens) {

        JPAQueryFactory qf = new JPAQueryFactory(entityManager);
        JPAQuery<Book> query = qf.selectFrom(QBook.book);

        for (String s : tokens) {
            String str = "%" + s + "%";
            BooleanExpression expression = QBook.book.title.like(str);
            query.where(expression);
        }

        return query;
    }


    // 기존 검색 (페이징 X)
    // 검색하고자 하는 query를 구문 분석하여 나뉘어진 토큰의 갯수에 맞게 동적으로 query를 생성하고자 함.
    public List<Book> findBooksByToken(List<String> tokens) {
        // "select * from books where TITLE_NM like '%token1%' and TITLE_NM like '%token2%' ... "

        JPAQueryFactory qf = new JPAQueryFactory(entityManager);
        JPAQuery<Book> query = qf.selectFrom(QBook.book);

        for (String s : tokens) {
            String str = "%" + s + "%";
            BooleanExpression expression = QBook.book.title.like(str);
            query.where(expression);
        }

        return query.fetch();
    }


}



