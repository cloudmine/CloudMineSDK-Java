package com.cloudmine.api.rest;

import com.cloudmine.api.exceptions.JsonConversionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/21/12, 1:42 PM
 */
public class JsonUtilities {
    private static final Logger LOG = LoggerFactory.getLogger(JsonUtilities.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    public static final String NULL_STRING = "\"\"";
    public static final String RFC1123_PATTERN =
            "EEE, dd MMM yyyyy HH:mm:ss ";
    public static final String TAB = "  ";
    public static final DateTimeFormatter RFC1123_FORMATTER = DateTimeFormat.forPattern(RFC1123_PATTERN);

    public static DateTime toDate(String dateString) {
        return RFC1123_FORMATTER.parseDateTime(dateString);
    }

    public static String addQuotes(String toQuote) {
        if(toQuote == null) {
            return NULL_STRING;
        }
        return "\"" + toQuote + "\"";
    }

    public static String jsonCollection(Json... jsonEntities) {
        StringBuilder json = new StringBuilder("{\n");
        String comma = "";
        for(Json jsonEntity : jsonEntities) {
            json.append(comma)
                    .append(TAB)
                    .append(jsonEntity.asJson());
            comma = ",\n";
        }
        json.append("\n}");
        return json.toString();
    }

    public static String mapToJson(Map<String, Object> map) throws JsonConversionException {
        try {
            return jsonMapper.writeValueAsString(map);
        } catch (IOException e) {
            LOG.error("Trouble writing json", e);
            throw new JsonConversionException(e);
        }
    }

    public static Map<String, Object> jsonToMap(String json) throws JsonConversionException {
        try {
            return jsonMapper.readValue(json, Map.class);
        } catch (IOException e) {
            LOG.error("Trouble reading json", e);
            throw new JsonConversionException("JSON: " + json, e);
        }
    }

    /**
     * Tests whether two json strings are equivalent; ignores formating and order. Expensive operation
     * as the strings are parsed to JsonNodes, which are compared.
     * @param first
     * @param second
     * @return
     * @throws JsonConversionException
     */
    public static boolean isJsonEquivalent(String first, String second) throws JsonConversionException {
        try {
            JsonNode node = jsonMapper.readValue(first, JsonNode.class);
            JsonNode secondNode = jsonMapper.readValue(second, JsonNode.class);
            return node.equals(secondNode);
        } catch (IOException e) {
            throw new JsonConversionException("Couldn't convert string to json: " + first, e);
        }
    }
}
