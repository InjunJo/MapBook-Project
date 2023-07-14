package com.scaling.libraryservice.commons.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CsvMerger {


    public static void main(String[] args) {

        // 입력 파일 경로와 패턴 입력
        String inputFolder = "C:\\teamScaling\\libBooks";
        // 출력 파일명 입력
        String outputFileName = "lib_books.csv";

        merge(inputFolder, outputFileName);
    }


    public static void merge(String inputFolder, String outputFileName) {

        File[] files = new File(inputFolder)
            .listFiles((dir, name) -> name.endsWith(".csv"));

        boolean headerSaved = false;

        Objects.requireNonNull(files);

        try (BufferedWriter writer = Files.newBufferedWriter(
            Paths.get(outputFileName), StandardCharsets.UTF_8)) {

            for (File file : files) {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

                // 헤더 처리
                if (!headerSaved) {
                    writeLine(writer,lines.get(0));
                    headerSaved = true;
                }
                
                // 본문 처리
                lines.stream().skip(1).forEach(line -> writeLine(writer, line));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLine(BufferedWriter writer, String line) {

        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}