package com.noir.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.noir.exception.AppException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonRepository<T> {

    private final File file;
    private final Class<T> type;
    private final ObjectMapper mapper;

    public JsonRepository(String filePath, Class<T> type) {
        this.file = new File(filePath);
        this.type = type;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public List<T> read() {
        try {
            var listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
            return mapper.readValue(file, listType);
        } catch (IOException e) {
            throw new AppException("Failed to read data: " + file.getName(), 500);
        }
    }

    public void write(List<T> data) {
        try {
            file.getParentFile().mkdirs();
            mapper.writeValue(file, data);
        } catch (IOException e) {
            throw new AppException("Failed to write data: " + file.getName(), 500);
        }
    }
}