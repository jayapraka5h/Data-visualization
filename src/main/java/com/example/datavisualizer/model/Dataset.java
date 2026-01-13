package com.example.datavisualizer.model;

import java.util.List;
import java.util.Map;

public class Dataset {
    private String fileName;
    private List<String> headers;
    private List<Map<String, Object>> rows;

    public Dataset(String fileName, List<String> headers, List<Map<String, Object>> rows) {
        this.fileName = fileName;
        this.headers = headers;
        this.rows = rows;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }
}
