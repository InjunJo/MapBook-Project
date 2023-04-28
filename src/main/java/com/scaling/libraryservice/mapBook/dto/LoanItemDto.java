package com.scaling.libraryservice.mapBook.dto;

import com.scaling.libraryservice.mapBook.domain.ApiObservable;
import com.scaling.libraryservice.mapBook.domain.ConfigureUriBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

@Getter
@RequiredArgsConstructor
@ToString
public class LoanItemDto implements ConfigureUriBuilder, ApiObservable {

    private Integer no;

    private Integer ranking;

    private String bookName;

    private Double isbn13;

    private Integer loan_count;

    private String classNo;
    private static final String API_URL = "http://data4library.kr/api/loanItemSrch";

    private static final ApiStatus apiStatus = new ApiStatus(API_URL,10);

    public LoanItemDto(JSONObject obj) {

        this.no = obj.getInt("no");
        this.ranking = Integer.parseInt(obj.getString("ranking"));
        this.bookName = obj.getString("bookname");
        this.isbn13 = Double.parseDouble(obj.getString("isbn13"));
        this.loan_count = Integer.parseInt(obj.getString("loan_count"));
        this.classNo = obj.getString("class_no");
    }

    @Override
    public UriComponentsBuilder configUriBuilder(String pageSize) {
        UriComponentsBuilder uriBuilder
            = UriComponentsBuilder.fromHttpUrl(API_URL)
            .queryParam("authKey","0f6d5c95011bddd3da9a0cc6975868d8293f79f0ed1c66e9cd84e54a43d4bb72")
            .queryParam("pageSize", pageSize)
            .queryParam("format","json");

        return uriBuilder;
    }

    @Override
    public String getApiUrl() {
        return API_URL;
    }

    @Override
    public ApiStatus getApiStatus() {
        return apiStatus;
    }
}
