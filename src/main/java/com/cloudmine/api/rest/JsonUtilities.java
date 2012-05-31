package com.cloudmine.api.rest;

import com.cloudmine.api.exceptions.JsonConversionException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/21/12, 1:42 PM
 */
public class JsonUtilities {
    private static final Logger LOG = LoggerFactory.getLogger(JsonUtilities.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    public static final DateFormat CLOUDMINE_DATE_FORMATTER = new CloudMineDateFormat();
    static {
        //Using a serializer instead of setting the DateFormat to get around string escape issues
        SimpleModule dateModule = new SimpleModule("DateModule", new Version(1, 0, 0, null));
        dateModule.addSerializer(new JsonSerializer<Date>() {

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
                jgen.writeRaw(":" + dateToJsonClass(value));
            }

            @Override
            public Class<Date> handledType() {
                return Date.class;
            }
        });

        jsonMapper.registerModule(dateModule);


    }
    public static final String NULL_STRING = "\"\"";

    public static final String TAB = "  ";
    public static final String CLASS_KEY = "__class__";
    public static final String DATE_CLASS = "datetime";
    public static final String TIME_KEY = "timestamp";

    public static String dateToJsonClass(Date date) {
        if(date == null) {
            return NULL_STRING;
        }
        long secondsTime = date.getTime() / 1000;
        return createJsonClass(DATE_CLASS, createJsonProperty(TIME_KEY, secondsTime));
    }

    public static String createJsonClass(String className, String... properties) {
        StringBuilder classBuilder =
                new StringBuilder("{\n")
                .append(createJsonProperty(CLASS_KEY, className)).append(",\n");
        String comma = "";
        for(String property : properties) {
            classBuilder.append(comma).append(property);
            comma = ",\n";
        }
        classBuilder.append("\n}");
        return classBuilder.toString();
    }

    public static Date jsonClassToDate(String json) throws JsonConversionException {
        try {
            return CLOUDMINE_DATE_FORMATTER.parse(json);
        } catch (ParseException e) {
            throw new JsonConversionException("Couldn't parse: " + json, e);
        }
    }


    public static String createJsonProperty(String key, String value) {
        return new StringBuilder(addQuotes(key)).append(":").append(addQuotes(value)).toString();
    }

    public static String createJsonProperty(String key, Number value) {
        return new StringBuilder(addQuotes(key)).append(":").append(value).toString();
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
     * Converts the given json to a JsonNode
     * @param json
     * @return
     */
    public static JsonNode getNode(String json) throws JsonConversionException {
        try {
            return jsonMapper.readValue(json, JsonNode.class);
        } catch (IOException e) {
            throw new JsonConversionException("JSON: " + json, e);
        }
    }

    public static JsonNode getNode(InputStream inputJson) throws JsonConversionException {
        try {
            return jsonMapper.readValue(inputJson, JsonNode.class);
        } catch (IOException e) {
            throw new JsonConversionException("Couldn't parse stream", e);
        }
    }

    public static JsonNodeFactory getNodeFactory() {
        return jsonMapper.getNodeFactory();
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
            JsonNode firstNode = jsonMapper.readTree(first);
            try {
                JsonNode secondNode = jsonMapper.readTree(second);
                return firstNode.equals(secondNode);
            } catch (IOException e) {
                throw new JsonConversionException("Couldn't convert second string to json: " + second, e);
            }
        } catch (IOException e) {
            throw new JsonConversionException("Couldn't convert first string to json: " + first, e);
        }
    }
}
