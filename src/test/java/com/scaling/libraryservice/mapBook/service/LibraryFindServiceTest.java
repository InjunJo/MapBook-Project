package com.scaling.libraryservice.mapBook.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.scaling.libraryservice.mapBook.dto.LibraryDto;
import com.scaling.libraryservice.mapBook.dto.ReqMapBookDto;
import com.scaling.libraryservice.mapBook.exception.LocationException;
import com.scaling.libraryservice.mapBook.repository.LibraryMetaRepository;
import com.scaling.libraryservice.mapBook.repository.LibraryRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class LibraryFindServiceTest {

    @Autowired
    private LibraryFindService libraryFindService;

    @BeforeEach
    void setUp() {
    }

    @Test @DisplayName("위도/경도 데이터만으로 주변 도서관을 검색")
    public void find_libraries_with_coordinate(){
        /* given */

        var dto = new ReqMapBookDto("9788089365210",34.802858, 126.702513,null,null);

        /* when */

        var result = libraryFindService.getNearByLibraries(dto);

        /* then */
        result.forEach(System.out::println);
        assertNotEquals(0,result.size());
    }

    @Test @DisplayName("직접 주소 선택으로 주변 도서관 검색")
    public void find_libraries_with_address(){
        /* given */
        var dto = new ReqMapBookDto("9788089365210",0.0,0.0,"경기도","성남시");

        /* when */

        var result = libraryFindService.getNearByLibraries(dto);

        /* then */
        /*result.forEach(System.out::println);*/
        assertNotEquals(0,result.size());
    }

    @Test @DisplayName("잘못된 위/경도로 주변 도서관 검색")
    public void find_libraries_error(){
        /* given */
        var dto = new ReqMapBookDto("9788089365210",38.74273402531946, 127.3437713197453,null,null);

        /* when */

        Executable e = () -> libraryFindService.getNearByLibraries(dto);

        /* then */
        assertThrows(LocationException.class,e);
    }

    @Test @DisplayName("커스텀으로 만든 지역 코드로 도서관 찾기")
    public void find_libraries_by_areaCd(){
        /* given */
        int areaCd = 27500;

        /* when */

        Executable e = () -> libraryFindService.getNearByLibraries(areaCd);

        /* then */

        assertDoesNotThrow(e);
    }

    @Test @DisplayName("커스텀으로 만든 지역 코드로 도서관 찾기 에러 발생")
    public void find_libraries_by_areaCd_error(){
        /* given */
        int areaCd = 1;

        /* when */

        Executable e = () -> libraryFindService.getNearByLibraries(areaCd);

        /* then */

        assertThrows(LocationException.class,e);
    }

}