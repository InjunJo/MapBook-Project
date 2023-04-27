package com.scaling.libraryservice.mapBook.util;

import com.scaling.libraryservice.mapBook.domain.ApiObservable;
import com.scaling.libraryservice.mapBook.domain.ConfigureUriBuilder;
import com.scaling.libraryservice.mapBook.dto.ApiStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.transaction.NotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CircuitBreaker {

    private final List<ApiObservable> observingConnections = new ArrayList<>();

    public void receiveError(ApiObservable observer,Exception e) {

        Objects.requireNonNull(observer);

        ApiStatus status = observer.getApiStatus();

        String apiUri = status.getApiUri();
        Integer errorCnt = status.getErrorCnt();

        if (observingConnections.contains(observer)) {

            status.upErrorCnt();

        } else {
            status.upErrorCnt();
            observingConnections.add(observer);
        }

        log.error("[{}] is received - request api url : [{}] , current error cnt : [{}]"
            ,e.getMessage(),apiUri, errorCnt);
    }

    public static void closeObserver(ApiStatus status) {
        status.closeAccess();
        log.info(status.getApiUri() + " is closed by api server error at [{}]",
            status.getClosedTime());
    }

}
