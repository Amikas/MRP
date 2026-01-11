// File: test/util/JsonUtilTest.java
package mrp.util;

import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilTest {

    @Test
    void testSerializeDeserializeMap() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "John");
        map.put("age", 30);
        map.put("active", true);

        String json = JsonUtil.toJson(map);
        assertNotNull(json);
        assertTrue(json.contains("John"));
        assertTrue(json.contains("30"));
        assertTrue(json.contains("true"));

        // Deserialize back to Map
        Map<String, Object> deserialized = JsonUtil.fromJson(json, Map.class);
        assertEquals("John", deserialized.get("name"));
        assertEquals(30, deserialized.get("age"));
        assertEquals(true, deserialized.get("active"));
    }

    @Test
    void testSerializeSimpleObject() throws Exception {
        TestObject obj = new TestObject();
        obj.name = "Test";
        obj.value = 42;

        String json = JsonUtil.toJson(obj);
        TestObject deserialized = JsonUtil.fromJson(json, TestObject.class);

        assertEquals("Test", deserialized.name);
        assertEquals(42, deserialized.value);
    }

    // Simple test class
    static class TestObject {
        public String name;
        public int value;

        public TestObject() {}
    }
}