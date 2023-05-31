package com.scaling.libraryservice.mapBook.service;

import com.scaling.libraryservice.commons.api.apiConnection.BExistConn;
import com.scaling.libraryservice.commons.api.service.DataProvider;
import com.scaling.libraryservice.commons.caching.CustomCacheable;
import com.scaling.libraryservice.commons.timer.Timer;
import com.scaling.libraryservice.mapBook.dto.ApiBookExistDto;
import com.scaling.libraryservice.mapBook.dto.LibraryDto;
import com.scaling.libraryservice.mapBook.dto.ReqMapBookDto;
import com.scaling.libraryservice.mapBook.dto.RespMapBookDto;
import com.scaling.libraryservice.mapBook.exception.OpenApiException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MapBookMatcher implements ApiRelatedService {

    private final DataProvider<ApiBookExistDto> dataProvider;

    /**
     * 사용자가 원하는 도서와 사용자 주변의 도서관을 조합하여 대출 가능한 도서관들의 정보를 반환 한다.
     *
     * @param nearByLibraries 사용자 주변의 도서관 정보 Dto List
     * @param reqMapBookDto   사용자가 요청한 도서 정보와 위치 정보를 담는 Dto
     * @return 대출 가능한 도서관 정보를 담은 응답 Dto를 List 형태로 반환 한다.
     */
    @Timer
    @CustomCacheable
    public List<RespMapBookDto> matchMapBooks(@NonNull List<BExistConn> bExistConns,
        List<LibraryDto> nearByLibraries, ReqMapBookDto reqMapBookDto) throws OpenApiException {

        if (bExistConns.isEmpty()) {
            return nearByLibraries.stream().map(l -> new RespMapBookDto(reqMapBookDto, l, "N"))
                .toList();
        }

        List<ApiBookExistDto> apiResults = dataProvider.provideDataList(bExistConns,10);

        return mappingLoanableLib(nearByLibraries, apiResults);
    }


    /**
     * 대출 가능 Api 응답을 주변 도서관 정보와 연결 하여 대출 가능한 주변 도서관 정보를 담은 List를 반환 한다.
     *
     * @param nearByLibraries 사용자 주변 도서관 정보를 담은 Dto
     * @param apiResults      api로 부터 응답 받은 해당 도서의 대출 가능 여부 데이터를 담은 Dto List
     * @return 대출 가능한 주변 도서관 정보에 대한 Dto List
     */

    private List<RespMapBookDto> mappingLoanableLib(List<LibraryDto> nearByLibraries,
        List<ApiBookExistDto> apiResults) {

        List<RespMapBookDto> result = new ArrayList<>();

        Map<Integer, ApiBookExistDto> bookExistMap = changeToMap(apiResults);

        for (LibraryDto l : nearByLibraries) {

            ApiBookExistDto apiBookExistDto = bookExistMap.get(l.getLibNo());

            if (apiBookExistDto != null) {
                result.add(new RespMapBookDto(apiBookExistDto, l));
            }
        }

        return result;
    }

    private Map<Integer, ApiBookExistDto> changeToMap(List<ApiBookExistDto> apiResults) {

        Map<Integer, ApiBookExistDto> bookExistMap = new HashMap<>();

        for (ApiBookExistDto dto : apiResults) {

            if (dto.isLoanAble()) {

                bookExistMap.put(Integer.valueOf(dto.getLibCode()), dto);
            }
        }

        return bookExistMap;
    }

}
