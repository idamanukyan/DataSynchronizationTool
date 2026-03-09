package com.example.datasynchronizationtool.service.sync;

import com.example.datasynchronizationtool.exception.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Engine for applying transformations to field values during synchronization.
 * Supports built-in transformations and custom transformation rules.
 */
@Component
public class TransformationEngine {

    private static final Logger log = LoggerFactory.getLogger(TransformationEngine.class);

    private final Map<String, Function<Object, Object>> builtInTransformations;

    public TransformationEngine() {
        this.builtInTransformations = new HashMap<>();
        registerBuiltInTransformations();
    }

    private void registerBuiltInTransformations() {
        // String transformations
        builtInTransformations.put("UPPERCASE", value -> value != null ? value.toString().toUpperCase() : null);
        builtInTransformations.put("LOWERCASE", value -> value != null ? value.toString().toLowerCase() : null);
        builtInTransformations.put("TRIM", value -> value != null ? value.toString().trim() : null);
        builtInTransformations.put("CAPITALIZE", this::capitalize);

        // Null handling
        builtInTransformations.put("NULL_TO_EMPTY", value -> value == null ? "" : value);
        builtInTransformations.put("EMPTY_TO_NULL", value -> (value != null && value.toString().isEmpty()) ? null : value);

        // Type conversions
        builtInTransformations.put("TO_STRING", value -> value != null ? value.toString() : null);
        builtInTransformations.put("TO_INTEGER", this::toInteger);
        builtInTransformations.put("TO_LONG", this::toLong);
        builtInTransformations.put("TO_DOUBLE", this::toDouble);
        builtInTransformations.put("TO_BOOLEAN", this::toBoolean);

        // Date transformations
        builtInTransformations.put("DATE_TO_ISO", this::dateToIso);
        builtInTransformations.put("DATETIME_TO_ISO", this::dateTimeToIso);
    }

    /**
     * Apply a transformation rule to a value.
     *
     * @param value          The value to transform
     * @param transformation The transformation rule (can be null for no transformation)
     * @return The transformed value
     */
    public Object applyTransformation(Object value, String transformation) {
        if (transformation == null || transformation.trim().isEmpty()) {
            return value;
        }

        String trimmedTransformation = transformation.trim();
        log.debug("Applying transformation '{}' to value: {}", trimmedTransformation, value);

        try {
            Object result = applyTransformationInternal(value, trimmedTransformation);
            log.debug("Transformation result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to apply transformation '{}' to value '{}': {}",
                    trimmedTransformation, value, e.getMessage());
            throw new InvalidConfigurationException(
                    "Transformation failed: " + trimmedTransformation + " - " + e.getMessage());
        }
    }

    private Object applyTransformationInternal(Object value, String transformation) {
        // Check for built-in transformation
        if (builtInTransformations.containsKey(transformation.toUpperCase())) {
            return builtInTransformations.get(transformation.toUpperCase()).apply(value);
        }

        // Check for parameterized transformations
        if (transformation.toUpperCase().startsWith("PREFIX:")) {
            String prefix = transformation.substring(7);
            return value != null ? prefix + value.toString() : prefix;
        }

        if (transformation.toUpperCase().startsWith("SUFFIX:")) {
            String suffix = transformation.substring(7);
            return value != null ? value.toString() + suffix : suffix;
        }

        if (transformation.toUpperCase().startsWith("REPLACE:")) {
            return applyReplace(value, transformation.substring(8));
        }

        if (transformation.toUpperCase().startsWith("SUBSTRING:")) {
            return applySubstring(value, transformation.substring(10));
        }

        if (transformation.toUpperCase().startsWith("DEFAULT:")) {
            String defaultValue = transformation.substring(8);
            return value == null ? defaultValue : value;
        }

        if (transformation.toUpperCase().startsWith("FORMAT_DATE:")) {
            return formatDate(value, transformation.substring(12));
        }

        if (transformation.toUpperCase().startsWith("CONCAT:")) {
            String concatValue = transformation.substring(7);
            return value != null ? value.toString() + concatValue : concatValue;
        }

        // Check for chained transformations (e.g., "TRIM|UPPERCASE")
        if (transformation.contains("|")) {
            return applyChainedTransformations(value, transformation);
        }

        // If no matching transformation, log warning and return original value
        log.warn("Unknown transformation '{}', returning original value", transformation);
        return value;
    }

    private Object applyChainedTransformations(Object value, String transformation) {
        String[] transformations = transformation.split("\\|");
        Object result = value;
        for (String t : transformations) {
            result = applyTransformationInternal(result, t.trim());
        }
        return result;
    }

    private Object applyReplace(Object value, String params) {
        if (value == null) return null;
        // Format: "old->new"
        String[] parts = params.split("->");
        if (parts.length != 2) {
            throw new InvalidConfigurationException("REPLACE transformation requires format: 'old->new'");
        }
        return value.toString().replace(parts[0], parts[1]);
    }

    private Object applySubstring(Object value, String params) {
        if (value == null) return null;
        String str = value.toString();
        // Format: "start,end" or "start"
        String[] parts = params.split(",");
        int start = Integer.parseInt(parts[0].trim());
        if (parts.length == 2) {
            int end = Integer.parseInt(parts[1].trim());
            return str.substring(Math.min(start, str.length()), Math.min(end, str.length()));
        }
        return str.substring(Math.min(start, str.length()));
    }

    private Object capitalize(Object value) {
        if (value == null) return null;
        String str = value.toString();
        if (str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private Object toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return value;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString().trim());
    }

    private Object toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return value;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString().trim());
    }

    private Object toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString().trim());
    }

    private Object toBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return value;
        String str = value.toString().trim().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str) || "y".equals(str);
    }

    private Object dateToIso(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return value.toString();
    }

    private Object dateTimeToIso(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return value.toString();
    }

    private Object formatDate(Object value, String pattern) {
        if (value == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(formatter);
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(formatter);
        }
        return value.toString();
    }

    /**
     * Check if a transformation rule is valid.
     */
    public boolean isValidTransformation(String transformation) {
        if (transformation == null || transformation.trim().isEmpty()) {
            return true; // No transformation is valid
        }

        String upper = transformation.toUpperCase().trim();

        // Check built-in transformations
        if (builtInTransformations.containsKey(upper)) {
            return true;
        }

        // Check parameterized transformations
        String[] parameterized = {"PREFIX:", "SUFFIX:", "REPLACE:", "SUBSTRING:", "DEFAULT:", "FORMAT_DATE:", "CONCAT:"};
        for (String prefix : parameterized) {
            if (upper.startsWith(prefix)) {
                return true;
            }
        }

        // Check for chained transformations
        if (transformation.contains("|")) {
            String[] parts = transformation.split("\\|");
            for (String part : parts) {
                if (!isValidTransformation(part.trim())) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}
