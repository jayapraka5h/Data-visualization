package com.example.datavisualizer.service;

import com.example.datavisualizer.model.Dataset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class DataService {

    public Dataset parseFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        if (filename.toLowerCase().endsWith(".csv")) {
            return parseCsv(file);
        } else if (filename.toLowerCase().endsWith(".xlsx")) {
            return parseExcel(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Please upload CSV or Excel (xlsx).");
        }
    }

    private Dataset parseCsv(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            List<String> headers = new ArrayList<>(csvParser.getHeaderMap().keySet());
            List<Map<String, Object>> rows = new ArrayList<>();

            for (CSVRecord csvRecord : csvParser) {
                Map<String, Object> row = new HashMap<>();
                for (String header : headers) {
                    row.put(header, parseValue(csvRecord.get(header)));
                }
                rows.add(row);
            }

            return new Dataset(file.getOriginalFilename(), headers, rows);
        }
    }

    private Dataset parseExcel(MultipartFile file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            List<String> headers = new ArrayList<>();
            List<Map<String, Object>> rows = new ArrayList<>();

            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                for (Cell cell : headerRow) {
                    headers.add(cell.getStringCellValue());
                }
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, Object> rowData = new HashMap<>();
                
                // Ensure we iterate correctly matching headers
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null) {
                         rowData.put(headers.get(i), getCellValue(cell));
                    } else {
                        rowData.put(headers.get(i), null);
                    }
                }
                rows.add(rowData);
            }

            return new Dataset(file.getOriginalFilename(), headers, rows);
        }
    }

    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return parseValue(cell.getStringCellValue());
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula(); // Or evaluate
            default:
                return null;
        }
    }

    // Helper to attempt numeric parsing for generic generic objects (useful for CSV where everything is text initially)
    private Object parseValue(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            if (value.contains(".")) {
                 return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
