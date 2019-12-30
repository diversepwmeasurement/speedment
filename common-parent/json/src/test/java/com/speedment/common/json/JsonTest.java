/*
 *
 * Copyright (c) 2006-2019, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.common.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author Emil Forslund
 */
@Execution(ExecutionMode.CONCURRENT)
final class JsonTest {
    
    @Test
    void testParse_String() {
        final String json = "{\"message\":\"Hello, World!\"}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertEquals("Hello, World!", map.get("message"));
    }
    
    @Test
    void testParse_String2() {
        final String json = "{\"title\" : \"Greetings!\", \"message\" : \"Hello, World!\"}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertEquals("Hello, World!", map.get("message"));
        assertEquals("Greetings!", map.get("title"));
    }
    
    @Test
    void testParse_Long() {
        final String json = "{\"id\" : 5678}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertEquals(5678L, map.get("id"));
    }
    
    @Test
    void testParse_NegativeLong() {
        final String json = "{\"id\" : -5678}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertEquals(-5678L, map.get("id"));
    }
    
    @Test
    void testParse_Double() {
        final String json = "{\"average\" : 0.6789}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertEquals(0.6789, map.get("average"));
    }
    
    @Test
    void testParse_NegativeDouble() {
        final String json = "{\"average\" : -0.6789}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertEquals(-0.6789, map.get("average"));
    }
    
    @Test
    void testParse_False() {
        final String json = "{\"condition\" : false}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertEquals(false, map.get("condition"));
    }
    
    @Test
    void testParse_True() {
        final String json = "{\"condition\" : true}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertEquals(true, map.get("condition"));
    }
    
    @Test
    void testParse_Null() {
        final String json = "{\"random\" : null}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertTrue(map.containsKey("random"));
        assertNull(map.get("random"));
    }
    
    @Test
    void testParse_Array() {
        final String json = "{\"items\" : [\"one\", \"two\", \"three\"]}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        final List<String> expected = Arrays.asList("one", "two", "three");
        assertNotNull(map);
        assertEquals(expected, map.get("items"));
    }
    
    @Test
    void testParse_ArrayOfObjects() {
        final String json = "{\"numbers\" : [{\"one\":1}, {\"two\":2}, {\"three\":3}]}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        
        assertNotNull(map);
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("numbers");
        
        assertEquals(1L, list.get(0).get("one"));
        assertEquals(2L, list.get(1).get("two"));
        assertEquals(3L, list.get(2).get("three"));
    }
    
    @Test
    void testParse_ArrayOfNegativeObjects() {
        final String json = "{\"numbers\" : [{\"one\":-1}, {\"two\":-2}, {\"three\":-3}]}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        
        assertNotNull(map);
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("numbers");
        
        assertEquals(-1L, list.get(0).get("one"));
        assertEquals(-2L, list.get(1).get("two"));
        assertEquals(-3L, list.get(2).get("three"));
    }

    @Test
    void testParse_EscapedString() {
        final String json = "{\"message\":\"Hello, \\\"World\\\"!\\n\"}";
        @SuppressWarnings("unchecked")
        final Map<String, Object> map = (Map<String, Object>) Json.fromJson(json);
        assertNotNull(map);
        assertEquals("Hello, \"World\"!\n", map.get("message"));
    }

    @TestFactory
    Stream<DynamicTest> toJson() {
        final Map<String, Object> testCases = toJsonTestCases();

        assertThrows(IllegalArgumentException.class, () -> Json.toJson(new RuntimeException()));

        return testCases.entrySet().stream().map(testCase -> dynamicTest(testCase.getKey(), () -> {
            final Object value = testCase.getValue();

            final String pretty = Json.toJson(value);
            assertNotNull(pretty);

            final String compressed = Json.toJson(value, false);
            assertNotNull(compressed);

            final OutputStream prettyOutput = new DataOutputStream(new ByteArrayOutputStream());
            final OutputStream compressedOutput = new DataOutputStream(new ByteArrayOutputStream());

            Json.toJson(value, prettyOutput);
            Json.toJson(value, compressedOutput, false);
        }));
    }

    private Map<String, Object> toJsonTestCases() {
        final Map<String, Object> testCases = new HashMap<>();

        final Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("name", "Michael");

        final List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);
        list.add(map);

        testCases.put("testing null serialization", null);
        testCases.put("testing map serialization", map);
        testCases.put("testing list serialization", list);
        testCases.put("testing double serialization", 1d);
        testCases.put("testing float serialization", 1f);
        testCases.put("testing long serialization", 1L);
        testCases.put("testing integer serialization", 1);
        testCases.put("testing short serialization", (short) 1);
        testCases.put("testing byte serialization", (byte) 1);
        testCases.put("testing true serialization", true);
        testCases.put("testing false serialization", false);

        return testCases;
    }

}
