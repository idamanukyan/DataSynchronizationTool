package com.example.datasynchronizationtool.service.sync;

import com.example.datasynchronizationtool.exception.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransformationEngine Tests")
class TransformationEngineTest {

    private TransformationEngine transformationEngine;

    @BeforeEach
    void setUp() {
        transformationEngine = new TransformationEngine();
    }

    @Nested
    @DisplayName("String Transformations")
    class StringTransformations {

        @Test
        @DisplayName("UPPERCASE transforms string to uppercase")
        void uppercase() {
            assertEquals("HELLO WORLD", transformationEngine.applyTransformation("hello world", "UPPERCASE"));
        }

        @Test
        @DisplayName("LOWERCASE transforms string to lowercase")
        void lowercase() {
            assertEquals("hello world", transformationEngine.applyTransformation("HELLO WORLD", "LOWERCASE"));
        }

        @Test
        @DisplayName("TRIM removes leading and trailing whitespace")
        void trim() {
            assertEquals("hello", transformationEngine.applyTransformation("  hello  ", "TRIM"));
        }

        @Test
        @DisplayName("CAPITALIZE capitalizes first letter only")
        void capitalize() {
            assertEquals("Hello world", transformationEngine.applyTransformation("HELLO WORLD", "CAPITALIZE"));
        }

        @Test
        @DisplayName("String transformations handle null values")
        void nullHandling() {
            assertNull(transformationEngine.applyTransformation(null, "UPPERCASE"));
            assertNull(transformationEngine.applyTransformation(null, "LOWERCASE"));
            assertNull(transformationEngine.applyTransformation(null, "TRIM"));
        }
    }

    @Nested
    @DisplayName("Null Handling Transformations")
    class NullHandlingTransformations {

        @Test
        @DisplayName("NULL_TO_EMPTY converts null to empty string")
        void nullToEmpty() {
            assertEquals("", transformationEngine.applyTransformation(null, "NULL_TO_EMPTY"));
            assertEquals("value", transformationEngine.applyTransformation("value", "NULL_TO_EMPTY"));
        }

        @Test
        @DisplayName("EMPTY_TO_NULL converts empty string to null")
        void emptyToNull() {
            assertNull(transformationEngine.applyTransformation("", "EMPTY_TO_NULL"));
            assertEquals("value", transformationEngine.applyTransformation("value", "EMPTY_TO_NULL"));
        }
    }

    @Nested
    @DisplayName("Type Conversion Transformations")
    class TypeConversions {

        @Test
        @DisplayName("TO_STRING converts various types to string")
        void toString_() {
            assertEquals("123", transformationEngine.applyTransformation(123, "TO_STRING"));
            assertEquals("true", transformationEngine.applyTransformation(true, "TO_STRING"));
            assertEquals("45.67", transformationEngine.applyTransformation(45.67, "TO_STRING"));
        }

        @Test
        @DisplayName("TO_INTEGER converts string to integer")
        void toInteger() {
            assertEquals(123, transformationEngine.applyTransformation("123", "TO_INTEGER"));
            assertEquals(456, transformationEngine.applyTransformation(456, "TO_INTEGER"));
            assertEquals(789, transformationEngine.applyTransformation(789L, "TO_INTEGER"));
        }

        @Test
        @DisplayName("TO_LONG converts string to long")
        void toLong() {
            assertEquals(123L, transformationEngine.applyTransformation("123", "TO_LONG"));
            assertEquals(456L, transformationEngine.applyTransformation(456, "TO_LONG"));
        }

        @Test
        @DisplayName("TO_DOUBLE converts string to double")
        void toDouble() {
            assertEquals(123.45, transformationEngine.applyTransformation("123.45", "TO_DOUBLE"));
            assertEquals(456.0, transformationEngine.applyTransformation(456, "TO_DOUBLE"));
        }

        @Test
        @DisplayName("TO_BOOLEAN converts various values to boolean")
        void toBoolean() {
            assertEquals(true, transformationEngine.applyTransformation("true", "TO_BOOLEAN"));
            assertEquals(true, transformationEngine.applyTransformation("1", "TO_BOOLEAN"));
            assertEquals(true, transformationEngine.applyTransformation("yes", "TO_BOOLEAN"));
            assertEquals(true, transformationEngine.applyTransformation("Y", "TO_BOOLEAN"));
            assertEquals(false, transformationEngine.applyTransformation("false", "TO_BOOLEAN"));
            assertEquals(false, transformationEngine.applyTransformation("0", "TO_BOOLEAN"));
            assertEquals(false, transformationEngine.applyTransformation("no", "TO_BOOLEAN"));
        }

        @Test
        @DisplayName("Type conversions handle null values")
        void nullHandling() {
            assertNull(transformationEngine.applyTransformation(null, "TO_STRING"));
            assertNull(transformationEngine.applyTransformation(null, "TO_INTEGER"));
            assertNull(transformationEngine.applyTransformation(null, "TO_LONG"));
            assertNull(transformationEngine.applyTransformation(null, "TO_DOUBLE"));
            assertNull(transformationEngine.applyTransformation(null, "TO_BOOLEAN"));
        }
    }

    @Nested
    @DisplayName("Date Transformations")
    class DateTransformations {

        @Test
        @DisplayName("DATE_TO_ISO formats LocalDate to ISO format")
        void dateToIso() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            assertEquals("2024-03-15", transformationEngine.applyTransformation(date, "DATE_TO_ISO"));
        }

        @Test
        @DisplayName("DATETIME_TO_ISO formats LocalDateTime to ISO format")
        void dateTimeToIso() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 3, 15, 10, 30, 45);
            assertEquals("2024-03-15T10:30:45", transformationEngine.applyTransformation(dateTime, "DATETIME_TO_ISO"));
        }

        @Test
        @DisplayName("FORMAT_DATE formats date with custom pattern")
        void formatDate() {
            LocalDate date = LocalDate.of(2024, 3, 15);
            assertEquals("15/03/2024", transformationEngine.applyTransformation(date, "FORMAT_DATE:dd/MM/yyyy"));
        }
    }

    @Nested
    @DisplayName("Parameterized Transformations")
    class ParameterizedTransformations {

        @Test
        @DisplayName("PREFIX adds prefix to value")
        void prefix() {
            // PREFIX preserves case from the parameter after the colon
            assertEquals("pre_value", transformationEngine.applyTransformation("value", "PREFIX:pre_"));
        }

        @Test
        @DisplayName("SUFFIX adds suffix to value")
        void suffix() {
            assertEquals("file.txt", transformationEngine.applyTransformation("file", "SUFFIX:.txt"));
        }

        @Test
        @DisplayName("REPLACE replaces substring")
        void replace() {
            assertEquals("hello universe", transformationEngine.applyTransformation("hello world", "REPLACE:world->universe"));
        }

        @Test
        @DisplayName("SUBSTRING extracts substring")
        void substring() {
            assertEquals("ell", transformationEngine.applyTransformation("hello", "SUBSTRING:1,4"));
            assertEquals("ello", transformationEngine.applyTransformation("hello", "SUBSTRING:1"));
        }

        @Test
        @DisplayName("DEFAULT provides default value for null")
        void defaultValue() {
            assertEquals("N/A", transformationEngine.applyTransformation(null, "DEFAULT:N/A"));
            assertEquals("value", transformationEngine.applyTransformation("value", "DEFAULT:N/A"));
        }

        @Test
        @DisplayName("CONCAT appends value")
        void concat() {
            assertEquals("Hello World", transformationEngine.applyTransformation("Hello", "CONCAT: World"));
        }
    }

    @Nested
    @DisplayName("Chained Transformations")
    class ChainedTransformations {

        @Test
        @DisplayName("Multiple transformations can be chained with pipe")
        void chainedTransformations() {
            assertEquals("HELLO", transformationEngine.applyTransformation("  hello  ", "TRIM|UPPERCASE"));
            assertEquals("Hello", transformationEngine.applyTransformation("  HELLO  ", "TRIM|CAPITALIZE"));
        }

        @Test
        @DisplayName("Complex chained transformations work correctly")
        void complexChain() {
            // Chain TRIM then UPPERCASE then add suffix
            assertEquals("HELLO.TXT", transformationEngine.applyTransformation("  hello  ", "TRIM|UPPERCASE|SUFFIX:.TXT"));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation")
    class EdgeCases {

        @Test
        @DisplayName("Null or empty transformation returns original value")
        void noTransformation() {
            assertEquals("value", transformationEngine.applyTransformation("value", null));
            assertEquals("value", transformationEngine.applyTransformation("value", ""));
            assertEquals("value", transformationEngine.applyTransformation("value", "  "));
        }

        @Test
        @DisplayName("Unknown transformation returns original value")
        void unknownTransformation() {
            assertEquals("value", transformationEngine.applyTransformation("value", "UNKNOWN_TRANSFORM"));
        }

        @Test
        @DisplayName("Case insensitive transformation names")
        void caseInsensitive() {
            assertEquals("HELLO", transformationEngine.applyTransformation("hello", "uppercase"));
            assertEquals("HELLO", transformationEngine.applyTransformation("hello", "Uppercase"));
        }

        @Test
        @DisplayName("Invalid REPLACE format throws exception")
        void invalidReplace() {
            assertThrows(InvalidConfigurationException.class,
                () -> transformationEngine.applyTransformation("value", "REPLACE:invalid"));
        }

        @Test
        @DisplayName("isValidTransformation returns correct results")
        void validationCheck() {
            assertTrue(transformationEngine.isValidTransformation("UPPERCASE"));
            assertTrue(transformationEngine.isValidTransformation("lowercase"));
            assertTrue(transformationEngine.isValidTransformation("PREFIX:test"));
            assertTrue(transformationEngine.isValidTransformation("TRIM|UPPERCASE"));
            assertTrue(transformationEngine.isValidTransformation(null));
            assertTrue(transformationEngine.isValidTransformation(""));
            assertFalse(transformationEngine.isValidTransformation("INVALID_TRANSFORM"));
        }
    }
}
