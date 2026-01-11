package mrp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class JsonUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Register Java 8 Date/Time module
        mapper.registerModule(new JavaTimeModule());
        // Configure to write dates as ISO-8601 strings instead of arrays
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // readable date format
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static String toJson(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return mapper.readValue(json, clazz);
    }
}