package com.scaling.libraryservice.commons.data;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CsvWriter<V> {

    public void writeToCsv(List<V> target, String outputPath) {
        try {
            Path path = Paths.get(outputPath);
            Writer writer;

            // file이 이미 존재하면 이어쓰기를 실시 한다.
            if (Files.exists(path)) {
                writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
            } else {
                //file이 없으면 새로 파일을 만들고 시작 한다.
                writer = Files.newBufferedWriter(path);
            }

            StatefulBeanToCsv<V> beanToCsv = new StatefulBeanToCsvBuilder<V>(writer)
                .withQuotechar('"')
                .build();

            beanToCsv.write(target);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
