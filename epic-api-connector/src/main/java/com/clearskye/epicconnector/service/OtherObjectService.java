package com.clearskye.epicconnector.service;

import static com.clearskye.epicconnector.utils.EpicConstants.CSV_DELIMITER;
import static com.clearskye.epicconnector.utils.EpicConstants.CSV_HEADERS;
import static com.clearskye.epicconnector.utils.EpicConstants.DEFAULT_MAX_RECORDS;
import static com.clearskye.epicconnector.utils.EpicConstants.DEFAULT_OFFSET;
import static com.clearskye.epicconnector.utils.EpicConstants.HEAD_ROW_COUNT;
import static com.clearskye.epicconnector.utils.EpicConstants.LEFT_BRACKET;
import static com.clearskye.epicconnector.utils.EpicConstants.MAX_RECORDS;
import static com.clearskye.epicconnector.utils.EpicConstants.OFFSET;
import static com.clearskye.epicconnector.utils.EpicConstants.PAGE_SIZE;
import static com.clearskye.epicconnector.utils.EpicConstants.POSSIBLE_DELIMITERS;
import static com.clearskye.epicconnector.utils.EpicConstants.RIGHT_BRACKET;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtherObjectService {
    /**
     * Object Mapper for the Epic User Utility Service.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Environment to access environment-specific properties.
     */
    private final Environment environment;

    /**
     * Builds a list of maps representing records from a CSV file.
     *
     * @param filepath The path to the CSV file to be read.
     * @param filter An optional filter to match specific records.
     * @param searchContext A map containing pagination settings. The keys can include:
     *                      - "pageSize" (optional): The number of records to return.
     *                      - "offset" (optional): The starting point (row number) from which to return records.
     * @return A list of maps where each map represents a record from the CSV file,
     * @throws Exception If an error occurs while reading the file or parsing the CSV data.
     */
    public List<Map<String, String>> buildObjectMaps(String filepath, String filter,
                                                     Map<String, String> searchContext) throws Exception {
        List<Map<String, String>> records = new ArrayList<>();

        int pageSize = DEFAULT_MAX_RECORDS;
        int offset = 0;
        if (searchContext!=null) {
            pageSize =
                    Integer.parseInt(Optional.ofNullable(searchContext.get(PAGE_SIZE)).orElse(Optional.ofNullable(environment.getProperty(MAX_RECORDS)).orElse(String.valueOf(DEFAULT_MAX_RECORDS))));
            offset = Integer.parseInt(Optional.ofNullable(searchContext.get(OFFSET)).orElse(DEFAULT_OFFSET));
        }
        FileReader fileReader = new FileReader(filepath);
        Map<String, Object> csvSettings = detectSettings(filepath);
        String[] headers = objectMapper.convertValue(csvSettings.get(CSV_HEADERS), new TypeReference<String[]>() {
        });
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        int rowCount = 0;
        int dataCount = 0;
        String line;
        while ((line = bufferedReader.readLine())!=null) {
            rowCount++;
            if (rowCount <= Integer.parseInt(String.valueOf(csvSettings.get(HEAD_ROW_COUNT)))) {
                continue;
            }
            Map<String, String> record = new HashMap<>();
            String[] data = line.split(LEFT_BRACKET+csvSettings.get(CSV_DELIMITER)+RIGHT_BRACKET);
            if (data.length > 1) {
                if (!data[0].isBlank() && !data[1].isEmpty()) {
                    if (filter!=null) {
                        if (data[0].equals(filter)) {
                            record.put(headers[0], data[0]);
                            record.put(headers[1], data[1]);
                            records.add(record);
                            break;
                        }
                    } else if (searchContext!=null) {
                        if (dataCount >= offset && dataCount < (pageSize + offset)) {
                            record.put(headers[0], data[0]);
                            record.put(headers[1], data[1]);
                            records.add(record);
                        }
                        dataCount++;
                        if(dataCount > (pageSize + offset)){
                            break;
                        }
                    } else {
                        record.put(headers[0], data[0]);
                        record.put(headers[1], data[1]);
                        records.add(record);
                    }
                }
            }
        }
        return records;
    }

    /**
     * Get the CSV settings.
     *
     * @param filePath The CSV file.
     * @return csvMap CSV settings.
     */
    public Map<String, Object> detectSettings(String filePath) throws Exception {
        Map<String, Object> csvMap = new HashMap<>();
        FileReader fileReader = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fileReader);
        String line = null;
        int rowCount = 0;
        while ((line = br.readLine())!=null) {
            rowCount++;
            for (String delimit : POSSIBLE_DELIMITERS) {
                if (line.contains(delimit)) {
                    csvMap.put(CSV_DELIMITER, delimit);
                    csvMap.put(CSV_HEADERS, line.split(Pattern.quote(delimit)));
                    csvMap.put(HEAD_ROW_COUNT, rowCount);
                    return csvMap;
                }
            }
        }
        return csvMap;
    }
}
