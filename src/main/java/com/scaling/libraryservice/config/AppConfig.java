package com.scaling.libraryservice.config;


import com.scaling.libraryservice.commons.api.service.BExistProvider;
import com.scaling.libraryservice.commons.api.service.DataProvider;
import com.scaling.libraryservice.commons.api.util.ApiQueryBinder;
import com.scaling.libraryservice.commons.api.util.ApiQuerySender;
import com.scaling.libraryservice.commons.api.util.binding.BindingStrategy;
import com.scaling.libraryservice.commons.api.util.binding.BookExistBinding;
import com.scaling.libraryservice.commons.api.util.binding.KakaoBookBinding;
import com.scaling.libraryservice.commons.circuitBreaker.CircuitBreaker;
import com.scaling.libraryservice.commons.circuitBreaker.QuerySendChecker;
import com.scaling.libraryservice.commons.circuitBreaker.RestorationChecker;
import com.scaling.libraryservice.commons.updater.dto.BookApiDto;
import com.scaling.libraryservice.commons.updater.service.KakaoBookApiService;
import com.scaling.libraryservice.mapBook.dto.ApiBookExistDto;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

    @Bean @Scope("prototype")
    public StopWatch stopWatch() {
        return new StopWatch();
    }

    @Bean
    public CircuitBreaker circuitBreaker(RestorationChecker restorationChecker) {

        return new CircuitBreaker(
            new ArrayList<>(),
            Executors.newScheduledThreadPool(1),
            new ConcurrentHashMap<>(),
            restorationChecker);
    }

    @Bean
    public RestTemplate restTemplateTimeOut() {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);

        return new RestTemplate(factory);
    }

    @Bean
    public RestTemplate restTemplate() {

        return new RestTemplate();
    }

    @Bean
    public ApiQuerySender apiQuerySenderTimeOut() {

        return new ApiQuerySender(restTemplateTimeOut());
    }

    @Bean
    public ApiQuerySender apiQuerySender() {

        return new ApiQuerySender(restTemplate());
    }

    @Bean
    public Komoran komoran() {

        return new Komoran(DEFAULT_MODEL.FULL);
    }

    @Bean
    public RestorationChecker restorationChecker() {

        return new QuerySendChecker(apiQuerySenderTimeOut());
    }


    @Bean
    public BindingStrategy<BookApiDto> kakaoBinding() {

        return new KakaoBookBinding();
    }

    @Bean
    public BindingStrategy<ApiBookExistDto> bookExistBinding() {

        return new BookExistBinding();
    }

    @Bean
    public KakaoBookApiService kakaoBookApiService() {

        ApiQueryBinder<BookApiDto> binder = new ApiQueryBinder<>(kakaoBinding());

        return new KakaoBookApiService(apiQuerySender(), binder);
    }

    @Bean
    public DataProvider<ApiBookExistDto> dataProvider(){

        ApiQueryBinder<ApiBookExistDto> binder = new ApiQueryBinder<>(bookExistBinding());

        return new BExistProvider(apiQuerySender(),binder);
    }


}

