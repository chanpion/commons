package com.chenpp.common.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author April.Chen
 * @date 2024/6/18 09:33
 */
public class CsvUtil {
    public static Logger log = LoggerFactory.getLogger(CsvUtil.class);

    public static final String DEFAULT_SEPARATOR = ",";

    public static void readCsv(String filePath) {
        readCsv(filePath, DEFAULT_SEPARATOR, StandardCharsets.UTF_8);
    }


    public static Pair<List<String>, List<Map<String, String>>> readCsv(String filePath, String separator, Charset charset) {
        try (Reader in = new InputStreamReader(new FileInputStream(filePath), charset);
             BufferedReader reader = new BufferedReader(in)) {

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setRecordSeparator(separator)
                    .setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build();
            CSVParser csvParser = csvFormat.parse(reader);
            List<String> headers = csvParser.getHeaderNames();
            List<Map<String, String>> lines = csvParser.stream().map(CSVRecord::toMap).collect(Collectors.toList());
            return Pair.of(headers, lines);
        } catch (FileNotFoundException e) {
            log.error("file not found", e);
        } catch (IOException e) {
            log.error("read csv file error", e);
        }
        return null;
    }

    public static void writeCsv(String filePath, Collection<?> records, String[] headers) {
        writeCsv(filePath, records, headers, DEFAULT_SEPARATOR, false);
    }

    public static void writeCsv(String filePath, Collection<?> records, String[] headers, String delimiter, boolean append) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .setRecordSeparator("\r\n")
                .setDelimiter(delimiter)
                .build();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, append), StandardCharsets.UTF_8))) {
            //防止乱码
            writer.write(new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, StandardCharsets.UTF_8));
            CSVPrinter csvPrinter = csvFormat.print(writer);
            csvPrinter.printRecords(records);
//            records.forEach(record -> {
//                try {
//                    Collection<?> list = (Collection) record;
//                    csvPrinter.printRecord(list);
//                } catch (IOException e) {
//                    log.error("writer csv error", e);
//                }
//            });
            csvPrinter.flush();
            csvPrinter.close();
        } catch (IOException e) {
            log.error("writer csv error", e);
        }
    }


    public static void writeCsv(String filePath, List<Map<String, Object>> records) {
        if (CollectionUtils.isNotEmpty(records)) {
            return;
        }
        String[] headers = records.get(0).keySet().toArray(new String[0]);
        writeCsv(filePath, records.stream().map(Map::values).collect(Collectors.toList()), headers, DEFAULT_SEPARATOR, false);
    }
}
