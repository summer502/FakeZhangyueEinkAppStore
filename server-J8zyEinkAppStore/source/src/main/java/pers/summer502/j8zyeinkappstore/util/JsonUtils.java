package pers.summer502.j8zyeinkappstore.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    private static final TypeReference<List<Object>> LIST_TYPE = new TypeReference<>() {
    };

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonUtils() {
    }

    public static JsonNode toTree(String jsonString) throws JsonProcessingException {
        return objectMapper.readTree(jsonString);
    }

    public static String toStr(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    public static String toPrettyStr(Object value) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(value);
    }

    public static <T> T toObject(String jsonString, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, clazz);
    }

    public static <T> T toObject(File file, Class<T> clazz) throws IOException {
        return objectMapper.readValue(file, clazz);
    }

    public static <T> T toObject(URL url, Class<T> clazz) throws IOException {
        return objectMapper.readValue(url, clazz);
    }

    public static <T> T toObject(InputStream is, Class<T> clazz) throws IOException {
        return objectMapper.readValue(is, clazz);
    }


    public static Map<String, Object> toMap(String jsonString) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, MAP_TYPE);
    }

    public static Map<String, Object> toMap(File file) throws IOException {
        return objectMapper.readValue(file, MAP_TYPE);
    }

    public static List<Object> toList(String jsonString) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, LIST_TYPE);
    }

    public static List<Object> toList(File file) throws IOException {
        return objectMapper.readValue(file, LIST_TYPE);
    }

    public static List<Object> toList(InputStream is) throws IOException {
        return objectMapper.readValue(is, LIST_TYPE);
    }

}
