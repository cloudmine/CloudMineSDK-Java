package com.cloudmine.api.rest;

import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/21/12, 1:42 PM
 */
public class JsonUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(JsonUtilities.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static final String EMPTY_JSON = "{ }";
    public static final DateFormat CLOUDMINE_DATE_FORMATTER = new CMDateFormat();
    static {
        //Using a serializer instead of setting the DateFormat to get around string escape issues
        SimpleModule dateModule = new SimpleModule("DateModule", new Version(1, 0, 0, null));
        dateModule.addSerializer(new JsonSerializer<Date>() {

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
                jgen.writeStartObject();
                jgen.writeRaw(dateToUnwrappedJsonClass(value));
                jgen.writeEndObject();
            }

            @Override
            public Class<Date> handledType() {
                return Date.class;
            }
        });

        dateModule.addSerializer(new JsonSerializer<SimpleCMObject>() {

            @Override
            public void serialize(SimpleCMObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeRaw(":");
                jgen.writeRaw(value.asUnkeyedObject());
            }

            @Override
            public Class<SimpleCMObject> handledType() {
                return SimpleCMObject.class;
            }
        });

        dateModule.addSerializer(new JsonSerializer<Json>() {
            @Override
            public void serialize(Json value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeRaw(":");
                jgen.writeRaw(value.asJson());
            }
            @Override
            public Class<Json> handledType() {
                return Json.class;
            }
        });

        jsonMapper.registerModule(dateModule);


    }
    public static final String NULL_STRING = "\"\"";

    public static final String TAB = "  ";
    public static final String CLASS_KEY = "__class__";
    public static final String TYPE_KEY = "__type__"; //type is used to identify objects that have special properties in CloudMine
    public static final String DATE_CLASS = "datetime";
    public static final String TIME_KEY = "timestamp";
    public static final String ENCODING = "UTF-8";

    public static String dateToUnwrappedJsonClass(Date date ){
        if(date == null) {
            return NULL_STRING;
        }
        long secondsTime = date.getTime() / 1000;

        return new StringBuilder(createJsonProperty(CLASS_KEY, DATE_CLASS)).append(",\n")
                .append(createJsonProperty(TIME_KEY, secondsTime)).toString();

    }

    public static String dateToJsonClass(Date date) {
        if(date == null) {
            return NULL_STRING;
        }
        return "{\n" + dateToUnwrappedJsonClass(date) + "\n}";
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

    public static Date jsonClassToDate(Map<String, Object> jsonMap) throws JsonConversionException {
        if(jsonMap == null ||
                DATE_CLASS.equals(jsonMap.get(CLASS_KEY))) {
            Object timeStamp = jsonMap.get(TIME_KEY);
            if(timeStamp != null) {
                try {
                    return CMDateFormat.fromNumber(Long.parseLong(timeStamp.toString()));
                } catch(NumberFormatException e) {
                    throw new JsonConversionException("Couldn't parse date", e);
                }
            }
        }
        throw new JsonConversionException("Given improper arguments to construct a date");
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

    public static Json jsonCollection(Collection<? extends Json> jsonEntities) {
        return jsonCollection(jsonEntities.toArray(new Json[jsonEntities.size()]));
    }

    public static Json jsonStringsCollection(Collection<String> jsonEntities) {
        return jsonCollection(jsonEntities.toArray(new String[jsonEntities.size()]));
    }

    public static Json jsonCollection(Json... jsonEntities) {
        String[] jsonStrings = new String[jsonEntities.length];
        for(int i = 0; i < jsonEntities.length; i++) {
            jsonStrings[i] = jsonEntities[i].asJson();
        }
        return jsonCollection(jsonStrings);
    }

    public static Json jsonCollection(String... jsonEntities) {
        StringBuilder json = new StringBuilder("{\n");
        String comma = "";
        for(String jsonEntity : jsonEntities) {
            json.append(comma)
                    .append(TAB)
                    .append(jsonEntity);
            comma = ",\n";
        }
        json.append("\n}");
        return new JsonString(json.toString());
    }

    public static String mapToJson(Map<String, Object> map) throws JsonConversionException {
        if(map == null) {
            return EMPTY_JSON;
        }
        try { //TODO this shit all brokes
            StringWriter writer = new StringWriter();
            JsonGenerator generator = jsonMapper.getJsonFactory().createJsonGenerator(writer);
            jsonMapper.writeValue(writer, map);
            return writer.toString();
        } catch (IOException e) {
            LOG.error("Trouble writing json", e);
            throw new JsonConversionException(e);
        }
    }

    public static Map<String, Object> jsonToMap(Json json) throws JsonConversionException {
        if(json == null)
            return new HashMap<String, Object>();
        return jsonToMap(json.asJson());
    }

    public static Map<String, Object> jsonToMap(String json) throws JsonConversionException {
        try {
            Map<String, Object> jsonMap = jsonMapper.readValue(json, Map.class);
            convertDateClassesToDates(jsonMap);
            return jsonMap;
        } catch (IOException e) {
            LOG.error("Trouble reading json", e);
            throw new JsonConversionException("JSON: " + json, e);
        }
    }

    /**
     * Replaces any json datetime objects with dates. Modifies the passed in map
     * @param jsonMap
     * @return
     * @throws JsonConversionException
     */
    private static Object convertDateClassesToDates(Map<String, Object> jsonMap) throws JsonConversionException {
        if(jsonMap == null)
            return null;
        boolean isDateClass = jsonMap.containsKey(CLASS_KEY) &&
                DATE_CLASS.equals(jsonMap.get(CLASS_KEY));
        if(isDateClass) {
            Object time = jsonMap.get(TIME_KEY);
            if(time instanceof Number) {
                return CMDateFormat.fromNumber((Number) time); //replace Number with Date
            } else {
                throw new JsonConversionException("Received non number time");
            }
        }

        for(Map.Entry<String, Object> jsonEntry : new HashMap<String, Object>(jsonMap).entrySet()) {
            if(jsonEntry.getValue() instanceof Map) {
                jsonMap.put(jsonEntry.getKey(),
                        convertDateClassesToDates((Map<String, Object>)jsonEntry.getValue()));
            }
        }
        return jsonMap;
    }

    public static Map<String, Object> jsonToMap(InputStream inputJson) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputJson, writer, ENCODING);
            return jsonToMap(writer.toString());
        } catch (IOException e) {
            throw new JsonConversionException("Couldn't read inputJson", e);
        }
    }

    public static boolean isJsonEquivalent(Json first, Json second) throws JsonConversionException {
        if(first == null)
            return second == null;
        if(second == null)
            return false;
        return isJsonEquivalent(first.asJson(), second.asJson());
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
