package com.scaling.libraryservice;

import com.google.gson.Gson;
import com.scaling.libraryservice.commons.apiConnection.BExistConn;
import com.scaling.libraryservice.commons.apiConnection.LoanItemConn;
import com.scaling.libraryservice.mapBook.cacheKey.HasBookCacheKey;
import com.scaling.libraryservice.mapBook.controller.MapBookController;
import com.scaling.libraryservice.mapBook.domain.ApiObserver;
import com.scaling.libraryservice.mapBook.dto.ReqMapBookDto;
import com.scaling.libraryservice.mapBook.dto.TestingBookDto;
import com.scaling.libraryservice.mapBook.util.ApiQueryBinder;
import com.scaling.libraryservice.mapBook.util.ApiQuerySender;
import com.scaling.libraryservice.search.util.TitleAnalyzer;
import com.scaling.libraryservice.search.util.TitleDivider;
import com.scaling.libraryservice.search.util.TitleTokenizer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class LearningTest {


    private TitleTokenizer titleTokenizer = new TitleTokenizer(new Komoran(DEFAULT_MODEL.FULL));

    @Test
    public void reflection(){
        /* given */

        /* when */
        var result = HasBookCacheKey.class.getDeclaredFields();
        /* then */

        System.out.println(result[0].getName());
    }

    @Test
    public void test() {

        ExecutorService service = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 10; i++) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " hello");
                }
            });
        }

        service.shutdown();
    }

    @Test
    public void thread(){

        try {
            for(int i=0; i<3; i++){
                Thread.sleep(5000);
                System.out.println("hello "+ DateTime.now());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } {

        }

    }

    @Test
    public void hashSet_List() {

        Map<String, String> map = new HashMap<>();

        var result = map.computeIfAbsent("a", s -> "A");

        System.out.println(result);
        System.out.println(map.get("a"));
    }

    @Test
    public void caffeine_cache() {
        /* given */

        ReqMapBookDto dto = new ReqMapBookDto("12345", 37.3, 124.4, null, null);
        ReqMapBookDto dto2 = new ReqMapBookDto("12345", 37.3, 124.4, null, null);


        /* when */

        System.out.println(dto.equals(dto2));

        /* then */
    }

    @Test
    public void lnp() {

        // Setup Stanford CoreNLP pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Input text
        String text = "(UML과 GoF 디자인 패턴 핵심 10가지로 배우는) JAVA 객체 지향 디자인 패턴";

        // Annotate text
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        Map<String, List<String>> result = new HashMap<>();


        // Extract tokens
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);

                if(TitleAnalyzer.isEnglish(word)){
                    System.out.println("eng : "+word);
                }else if(TitleAnalyzer.isKorean(word)){
                    System.out.println("kor : "+word);
                }

            }
        }


    }

    @Test
    public void isEnglish(){
        /* given */
        var result = TitleAnalyzer.isEnglish("mysql 8.0");
        /* when */
        System.out.println(result);
        /* then */
    }

    @Test
    public void english_korean() {
        String text = "e-mail에 꼭 필요한 알짜표현";

        var result = TitleDivider.divideTitle(text);

        System.out.println(result);
    }

    @Test
    public void english_korean2() {
        String text = "HTML5, CSS3, Javascript 모바일 웹";

        char[] chars = text.toCharArray();

        StringBuilder engBuffer = new StringBuilder();
        StringBuilder korBuffer = new StringBuilder();

        List<String> eng = new ArrayList<>();
        List<String> kor = new ArrayList<>();

        boolean engFirst = true;
        boolean korFirst = true;

        for (char c : chars) {

            String pattern = "^[a-zA-Z\\s+]+$";

            if ((c+"").matches(pattern)) {
                if (!korBuffer.isEmpty()) {
                    kor.add(korBuffer.toString());
                    korBuffer.setLength(0);

                }

                if (c == ' ' & engFirst) {

                } else {
                    engBuffer.append(c);
                    engFirst = false;
                }

            } else  {

                if (!engBuffer.isEmpty()) {

                    eng.add(engBuffer.toString());
                    engBuffer.setLength(0);
                }

                if (c == ' ' & korFirst) {

                } else {
                    korBuffer.append(c);
                    korFirst = false;
                }

            }
        }

        if (!engBuffer.isEmpty()) {
            eng.add(engBuffer.toString());
        }

        if (!korBuffer.isEmpty()) {
            kor.add(korBuffer.toString());
        }

        Map<String, List<String>> result = new HashMap<>();

        result.put("eng", eng);
        result.put("kor", kor);

        System.out.println(result);

    }

    @Test
    public void tokenizer() {
        /* given */
        String text = "Easy web publishing with HTML";
        /* when */
        var result = titleTokenizer.tokenize(text);
        /* then */

        System.out.println();

        System.out.println(result);
    }

    @Test
    public void reflection2()
        throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException {
        /* given */


        Method substitute = null;

        for (Method m : MapBookController.class.getMethods()){

            if(m.getName().contains("getHasBookMarkers")){
                substitute = m;
            }
        }

        Class<?> oClazz = BExistConn.class;

        Field field = BExistConn.class.getDeclaredField("apiStatus");
        ApiObserver apiStatus = (ApiObserver) oClazz.getConstructor().newInstance();

        System.out.println(apiStatus.getApiStatus().getApiUri());

    }

    @Test
    public void testing(){
        /* given */
        ApiQueryBinder apiQueryBinder = new ApiQueryBinder();
        ApiQuerySender sender = new ApiQuerySender(new RestTemplate());
        /* when */

        var reulst = sender.singleQueryJson(new LoanItemConn(),String.valueOf(5000));

        /* then */

        var result = apiQueryBinder.bindLoanItem(reulst).stream().map(l -> new TestingBookDto(l.getBookName())).toList();

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("book",result);

        Gson gson = new Gson();



        String filePath = "test_book.ser";

        try (FileWriter fw = new FileWriter(filePath)) {
            fw.write(gson.toJson(result));

        } catch (IOException e) {

        }
        

    }


}
