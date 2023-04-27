package com.scaling.libraryservice.mapBook.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.scaling.libraryservice.aop.Timer;
import com.scaling.libraryservice.mapBook.dto.ApiBookExistDto;
import com.scaling.libraryservice.mapBook.dto.ApiStatus;
import com.scaling.libraryservice.mapBook.dto.LibraryDto;
import com.scaling.libraryservice.mapBook.dto.ReqMapBookDto;
import com.scaling.libraryservice.mapBook.dto.RespMapBookDto;
import com.scaling.libraryservice.mapBook.util.ApiQueryBinder;
import com.scaling.libraryservice.mapBook.util.ApiQuerySender;
import com.scaling.libraryservice.mapBook.util.MapBookApiHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CachedMapBookManager {

    private Cache<ReqMapBookDto, List<RespMapBookDto>> mapBookCache;
    private final LibraryFindService libraryFindService;
    private final ApiQuerySender apiQuerySender;
    private final ApiQueryBinder apiQueryBinder;
    private final MapBookApiHandler mapBookApiHandler;

    @PostConstruct
    public void init() {
        this.mapBookCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();

    }

    @Timer
    public List<RespMapBookDto> getMapBooks(ReqMapBookDto mapBookDto) {

        List<RespMapBookDto> value = mapBookCache.getIfPresent(mapBookDto);

        if (value != null) {

            log.info(mapBookDto.getIsbn() + "  cache hit !!!!!!!!!!!!!!!!");

        } else {

            log.info(mapBookDto.getIsbn() + "  cache miss...............");

            List<LibraryDto> nearByLibraries = libraryFindService.getNearByLibraries(mapBookDto);

            if (!nearByLibraries.isEmpty()) {

                ApiStatus status = nearByLibraries.get(0).getApiStatus();

                if (!status.apiAccessible()) {

                    log.info("{} is not accessible then Service is changed hasBookLib service",status.getApiUri());

                    return nearByLibraries.stream().map(RespMapBookDto::new).toList();
                } else {

                    Map<Integer, ApiBookExistDto> bookExistMap = apiQueryBinder.bindBookExistMap(
                        apiQuerySender.multiQuery(
                            nearByLibraries,
                            mapBookDto.getIsbn(),
                            nearByLibraries.size()));

                    value = mapBookApiHandler.matchMapBooks(nearByLibraries, bookExistMap);
                }
            } else {
                value = new ArrayList<>();
            }

            mapBookCache.put(mapBookDto, value);
        }

        return value;
    }


}
