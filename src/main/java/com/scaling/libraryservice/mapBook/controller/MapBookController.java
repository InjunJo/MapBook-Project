package com.scaling.libraryservice.mapBook.controller;

import com.scaling.libraryservice.commons.apiConnection.BExistConn;
import com.scaling.libraryservice.commons.circuitBreaker.ApiMonitoring;
import com.scaling.libraryservice.mapBook.dto.LibraryDto;
import com.scaling.libraryservice.mapBook.dto.ReqMapBookDto;
import com.scaling.libraryservice.mapBook.dto.RespMapBookDto;
import com.scaling.libraryservice.mapBook.exception.OpenApiException;
import com.scaling.libraryservice.mapBook.service.LibraryFindService;
import com.scaling.libraryservice.mapBook.service.MapBookService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MapBookController {

    private final MapBookService mapBookService;

    private final LibraryFindService libraryFindService;


    /**
     * 사용자의 요청에 맞는 대출 가능 도서관 마커 데이터를 model에 담아 view에 전달 합니다
     *
     * @param model View에 전달 할 marker 데이터를 담는 Model 객체
     * @param reqMapBookDto 요청 받은 도서와 사용자의 위치 정보가 담겨 있는 Dto
     * @return Model을 전달 받고 View를 구성 할 html 파일 이름
     */

    @PostMapping("/books/mapBook/search")
    @ApiMonitoring(api = BExistConn.class, substitute = "getHasBookMarkers")
    public String getMapBooks(ModelMap model, @RequestBody ReqMapBookDto reqMapBookDto) {

        List<LibraryDto> nearbyLibraries = libraryFindService.getNearByLibraries(reqMapBookDto);

        List<RespMapBookDto> mapBooks = mapBookService.matchMapBooks(nearbyLibraries,
            reqMapBookDto);

        model.put("mapBooks", mapBooks);

        return "mapBook/mapBookMarker";
    }

    /**
     * Circuit breaker 판단에 의해 필요시 getMapBooks method 대신 호출 합니다
     * @param model View에 전달 할 소장 도서관 마커 데이터를 담는 Model 객체
     * @param reqMapBookDto getMapBooks moethod에게 전달 받은 사용자 요청 데이터가 담긴 Dto
     * @return Model을 전달 받고 View를 구성 할 html 파일 이름
     */
    public String getHasBookMarkers(ModelMap model,
        @ModelAttribute ReqMapBookDto reqMapBookDto) {

        List<LibraryDto> nearbyLibraries = libraryFindService.getNearByLibraries(reqMapBookDto);

        List<RespMapBookDto> hasBookLibs = nearbyLibraries.stream().map(l -> new RespMapBookDto(reqMapBookDto,l,"N"))
            .toList();

        model.put("hasBookLibs", hasBookLibs);

        return "mapBook/hasLibMarker";
    }


}