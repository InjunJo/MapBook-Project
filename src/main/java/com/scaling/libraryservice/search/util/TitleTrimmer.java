package com.scaling.libraryservice.search.util;

import java.util.Arrays;
import java.util.stream.Collectors;


public class TitleTrimmer {

    public static String removeKeyword(String query) {

        String[] keyWord = {"이야기", "장편소설", "한국사"};

        if (query.split(" ").length > 1) {

            for (String key : keyWord) {
                query = query.replaceAll(key, "");
            }
        }

        return query.trim();
    }


    /**
     * 주어진 제목 문자열을 나눈 뒤 다른 문자를 더해 알맞게 변형 합니다.
     *
     * @param target 변형하고자 하는 제목 문자열
     * @return 변형된 제목 문자열
     */
    public static String splitAddPlus(String target) {
        return Arrays.stream(target.split(" "))
            .map(name -> "+" + name)
            .collect(Collectors.joining(" "));
    }

    /**
     * 도서 제목에서 불필요한 불용어를 제거하여 일정한 형태로 반환합니다.
     *
     * @param title 제거하고자 하는 도서 제목 문자열
     * @return 불용어가 제거된 도서 제목 문자열
     */
    public static String TrimTitleResult(String title) {
        String[] titleParts = title.split(":");
        if (titleParts.length > 1) {
            String titlePrefix = titleParts[0];
            String[] titlePrefixParts = titlePrefix.trim().split("=");
            if (titlePrefixParts.length > 0) {
                titlePrefix = titlePrefixParts[0].trim();
            }
            return removeParentheses(removeDash(titlePrefix)).trim();
        }
        return removeParentheses(removeDash(title)).trim();
    }

    private static String removeParentheses(String text) {
        return text.replaceAll("\\(.*?\\)|=.*$", "").trim();
    }

    private static String removeDash(String text) {
        return text.replaceAll("-.*$", "").trim();
    }

}
