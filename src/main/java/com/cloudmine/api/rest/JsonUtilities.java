package com.cloudmine.api.rest;

import com.cloudmine.api.CMObject;
import com.cloudmine.api.CMUser;
import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.persistance.CMJacksonModule;
import com.cloudmine.api.persistance.CMUserConstructorMixIn;
import com.cloudmine.api.persistance.ClassNameRegistry;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * Simplify working with JSON by putting all the utility methods in one place. Mostly focused on converting
 * objects to and from JSON
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class JsonUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(JsonUtilities.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static final String EMPTY_JSON = "{ }";
    public static final DateFormat CLOUDMINE_DATE_FORMATTER = new CMDateFormat();
    static {
        SimpleModule customModule = new CMJacksonModule();

        jsonMapper.registerModule(customModule);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static final String NULL_STRING = "\"\"";

    public static final String TAB = "  ";
    public static final String CLASS_KEY = "__class__";
    public static final String TYPE_KEY = "__type__"; //type is used to identify objects that have special properties in CloudMine
    public static final String OBJECT_ID_KEY = "__id__";
    public static final String DATE_CLASS = "datetime";
    public static final String TIME_KEY = "timestamp";
    public static final String ENCODING = "UTF-8";

    /**
     * Convert a {@link Date} to an unwrapped CloudMine date object. Unwrapped means it is not surrounded by { }
     * @param date the Date to convert. If null, an empty string "" is returned
     * @return the date as JSON, or "" if given null
     */
    public static String convertDateToUnwrappedJsonClass(Date date){
        if(date == null) {
            return NULL_STRING;
        }
        long secondsTime = date.getTime() / 1000;

        return new StringBuilder(createJsonProperty(CLASS_KEY, DATE_CLASS)).append(",\n")
                .append(createJsonProperty(TIME_KEY, secondsTime)).toString();

    }

    public static void addCMUserMixinsTo(Class klass) {
        if(CMUser.class.isAssignableFrom(klass)) {
            jsonMapper.addMixInAnnotations(klass, CMUserConstructorMixIn.class);
        }
    }

    /**
     * Convert a {@link Date} to a CloudMine date object
     * @param date the Date to convert. If null, a wrapped empty string {\n""\n} is returned
     * @return the date as a JSON object, or {""} if given null
     */
    public static String convertDateToJsonClass(Date date) {
        String unwrappedDate = convertDateToUnwrappedJsonClass(date);
        return "{\n" + unwrappedDate + "\n}";
    }

    /**
     * Convert a key and value to its JSON representation
     * @param key the JSON key
     * @param value the JSON value
     * @return "key":"value"
     */
    public static String createJsonProperty(String key, String value) {
        return new StringBuilder(addQuotes(key)).append(":").append(addQuotes(value)).toString();
    }

    /**
     * Convert a key and value to oits JSON representation
     * @param key the JSON key
     * @param value the JSON value
     * @return "key":value
     */
    public static String createJsonProperty(String key, Number value) {
        return new StringBuilder(addQuotes(key)).append(":").append(value).toString();
    }

    /**
     * Remove the first "{" and last "}" from a JSON string
     * @param json a valid JSON string
     * @return if json == null, an empty string. if json does not contain an opening and closing brace, the passed in string. Otherwise,
     *              the passed in JSON with the first and last { and } removed
     */
    public static String unwrap(String json) {
        if(json == null) {
            return "";
        }
        int openBraces = json.indexOf("{");
        int closeBraces = json.lastIndexOf("}");
        if(openBraces < 0 || closeBraces < 0) {
            LOG.error("Given json to unwrap that does not have braces: " + json);
            return json;
        }
        String preOpen = json.substring(0, openBraces);
        String betweenBraces = json.substring(openBraces + 1, closeBraces);
        String postClose = json.substring(closeBraces + 1, json.length());
        String unwrappedJson = preOpen + //everything before the first open brace
                betweenBraces + //everything between the first place and the last closing brace
                postClose; //everything after the last closing brace
        return unwrappedJson;
    }

    /**
     * Add a leading and trailing "{" and "}" to the given string
     * @param json
     * @return
     */
    public static String wrap(String json) {
        if(json == null) {
            return EMPTY_JSON;
        }
        return new StringBuilder("{").append(json).append("}").toString();
    }

    /**
     * Quote a string
     * @param toQuote the value to quote
     * @return "toQuote"
     */
    public static String addQuotes(String toQuote) {
        if(toQuote == null) {
            return NULL_STRING;
        }
        return "\"" + toQuote + "\"";
    }

    /**
     * Enclose all the passed in jsonEntities in a JSON collection
     * @param jsonEntities to put into the collection
     * @return { jsonEntities[0].asJson, jsonEntities[1].asJson, ...}
     */
    public static Json jsonCollection(Collection<? extends Json> jsonEntities) {
        return jsonCollection(jsonEntities.toArray(new Json[jsonEntities.size()]));
    }

    /**
     * Enclose all the passed in strings in a JSON collection
     * @param jsonEntities JSON strings to put in the collection
     * @return { jsonEntities[0], jsonEntities[1], ...}
     */
    public static Json jsonStringsCollection(Collection<String> jsonEntities) {
        return jsonCollection(jsonEntities.toArray(new String[jsonEntities.size()]));
    }

    /**
     * Enclose all the passed in jsonEntities in a JSON collection
     * @param jsonEntities to put into the collection
     * @return { jsonEntities[0].asJson, jsonEntities[1].asJson, ...}
     */
    public static Json jsonCollection(Json... jsonEntities) {
        String[] jsonStrings = new String[jsonEntities.length];
        for(int i = 0; i < jsonEntities.length; i++) {
            jsonStrings[i] = jsonEntities[i].asJson();
        }
        return jsonCollection(jsonStrings);
    }

    /**
     * Enclose all the passed in strings in a JSON collection
     * @param jsonEntities JSON strings to put in the collection
     * @return { jsonEntities[0], jsonEntities[1], ...}
     */
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

    /**
     * Convert a Map to its representation as a JSON string.
     * @param map will be converted to its JSON representation
     * @return valid JSON that represents the passed in map. It should be true that map.equals(jsonToMap(mapToJson(map)))
     * @throws JsonConversionException if unable to convert this Map to json. This should never happen
     */
    public static String mapToJson(Map<String, ? extends Object> map) throws JsonConversionException {
        if(map == null) {
            return EMPTY_JSON;
        }
        StringWriter writer = new StringWriter();
        try {
            jsonMapper.writeValue(writer, map);
            return writer.toString();
        } catch (IOException e) {
            LOG.error("Trouble writing json", e);
            throw new JsonConversionException(e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //nope don't care
            }
        }
    }

    /**
     * Convert a CMObject to its JSON representation
     * @param objects the objects to convert
     * @return valid JSON that represents the passed in objects as a collection of JSON
     * @throws JsonConversionException if unable to convert this CMObject to json
     */
    public static String objectsToJson(CMObject... objects) throws JsonConversionException {
        if(objects == null) {
            LOG.debug("Received null objects, returning empty json");
            return EMPTY_JSON;
        }
        Map<String, CMObject> objectMap = new HashMap<String, CMObject>();
        for(CMObject object : objects) {
            objectMap.put(object.getObjectId(), object);
        }

        StringWriter writer = new StringWriter();
        try {

            jsonMapper.writeValue(writer, objectMap);
            return writer.toString();
        } catch(IOException e) {
            LOG.error("Trouble writing json", e);
            throw new JsonConversionException(e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //nope don't care
            }
        }
    }

    public static String objectToJson(CMObject object) throws JsonConversionException {
        StringWriter writer = new StringWriter();
        try {
            jsonMapper.writeValue(writer, object);
            return writer.toString();
        } catch (IOException e) {
            LOG.error("Exception thrown", e);
            throw new JsonConversionException(e);
        }
    }

    /**
     * Convert the given JSON to the given klass. If unable to convert, throws JsonConversionException
     * @param json JSON representing
     * @param klass
     * @param <CMO>
     * @return
     * @throws JsonConversionException
     */
    public static <CMO> CMO jsonToClass(String json, Class<CMO> klass) throws JsonConversionException {
        try {
            CMO object = jsonMapper.readValue(json, klass);
            return object;
        }catch (IOException e) {
            LOG.error("Trouble reading json", e);
            throw new JsonConversionException("JSON: " + json, e);
        }
    }

    public static CMObject jsonToClass(String json) throws JsonConversionException {
        Map<String, Object> jsonMap = jsonToMap(json); //this is a slow but easy way to get the klass name, might have to be replaced in the future
        Object klassString = jsonMap.get(CLASS_KEY);
        if(klassString == null ||
                ClassNameRegistry.isRegistered(klassString.toString()) == false) {
            return SimpleCMObject.SimpleCMObject(new JsonString(json));
        }
        Class<? extends CMObject> klass = ClassNameRegistry.forName(klassString.toString());
        return jsonToClass(json, klass);
    }

    /**
     * Convert a Json entity to a Map representation
     * @param json valid JSON
     * @return If json is null, returns an empty Map. Otherwise, a Map whose keys are JSON Strings and whose values are JSON values
     * @throws JsonConversionException if unable to convert the given json to a map. Will happen if the asJson call fails or if unable to represent the json as a map
     */
    public static Map<String, Object> jsonToMap(Json json) throws JsonConversionException {
        if(json == null)
            return new HashMap<String, Object>();
        return jsonToMap(json.asJson());
    }

    /**
     * Convert a JSON string to a Map representation
     * @param json valid JSON
     * @return If json is null, returns an empty Map. Otherwise, a Map whose keys are JSON Strings and whose values are JSON values
     * @throws JsonConversionException if unable to convert the given json to a map. Will happen if the asJson call fails or if unable to represent the json as a map
     */
    public static Map<String, Object> jsonToMap(String json) throws JsonConversionException {
        Map<String, Object> jsonMap = jsonToClassMap(json, Object.class);
        convertDateClassesToDates(jsonMap);
        return jsonMap;
    }

    /**
     * Convert a JSON collection in the form { "key":{values...}, "anotherKey":{moreValues} } to a Map of key's to
     * objects, of the given klass.
     * @param json the JSON to convert
     * @param klass the
     * @param <CMO>
     * @return
     * @throws JsonConversionException
     */
    public static <CMO> Map<String, CMO> jsonToClassMap(String json, Class<CMO> klass) throws JsonConversionException {
        try {
            MapType mapType = jsonMapper.getTypeFactory().constructMapType(Map.class, String.class, klass);
            Map<String, CMO> jsonMap = jsonMapper.readValue(json, mapType);
            return jsonMap;
        } catch (IOException e) {
            LOG.error("Trouble reading json", e);
            throw new JsonConversionException("JSON: " + json, e);
        }
    }

    public static Map<String, CMObject> jsonToClassMap(String json) {
        Map<String, String> simpleMap = jsonMapToKeyMap(json);
        Map<String, CMObject> objectMap = new HashMap<String, CMObject>();
        for(Map.Entry<String, String> entry : simpleMap.entrySet()) {
            objectMap.put(entry.getKey(), jsonToClass(entry.getValue()));
        }
        return objectMap;
    }

    public static Map<String, String> jsonMapToKeyMap(String json) {
        //TODO this method is big and kinda gross
        try {
            StringReader reader = new StringReader(json);
            int readInt;
            int open = 0;
            boolean inString = false;
            Map<String, String> jsonMap = new HashMap<String, String>();
            StringBuilder keyBuilder = new StringBuilder();
            StringBuilder contentsBuilder = new StringBuilder();

            while((readInt = reader.read()) != -1) {
                char read = (char)readInt;
                switch(read) {
                    case '{':
                        if(!inString)
                            open++;
                        break;
                    case '}':
                        if(!inString) {
                            open--;
                            if(open == 1) { //we closed a full block
                                //finish off the recording
                                contentsBuilder.append(read);
                                //get the key
                                String key = keyBuilder.toString();
                                String[] splitKey = key.split("\"");
                                if(splitKey.length < 1) {
                                    throw new JsonConversionException("Missing key at: " + key);
                                }
                                String parsedKey = splitKey[1];
                                //get the contents
                                String contents = contentsBuilder.toString();
                                jsonMap.put(parsedKey, contents);
                                //reset
                                keyBuilder = new StringBuilder();
                                contentsBuilder = new StringBuilder();
                                continue;
                            }
                        }
                        break;
                    case '\"':
                        inString = !inString;

                }
                if(open == 1) {
                    keyBuilder.append(read);
                }else if(open > 1) {
                    contentsBuilder.append(read);
                }
            }
            return jsonMap;
        } catch (IOException e) {
            LOG.error("Exception thrown", e);
            throw new JsonConversionException("Trouble reading JSON: " + e);
        }
    }

    public static void mergeJsonUpdates(CMObject objectToUpdate, String json) throws JsonConversionException {
        try {
            jsonMapper.readerForUpdating(objectToUpdate).readValue(json);
        } catch (IOException e) {
            LOG.error("Exception thrown while merging json update: " + json, e);
            throw new JsonConversionException(e);
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

    /**
     * Convert an InputStream containg JSON to a Map representation
     * @param inputJson a stream of valid JSON
     * @return If json is null, returns an empty Map. Otherwise, a Map whose keys are JSON Strings and whose values are JSON values
     * @throws JsonConversionException if unable to convert the given json to a map. Will happen if the asJson call fails or if unable to represent the json as a map
     */
    public static Map<String, Object> jsonToMap(InputStream inputJson) throws JsonConversionException {
        if(inputJson == null) {
            return new HashMap<String, Object>();
        }
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputJson, writer, ENCODING);
            return jsonToMap(writer.toString());
        } catch (IOException e) {
            throw new JsonConversionException("Couldn't read inputJson", e);
        }
    }

    /**
     * Tests whether two json strings are equivalent; ignores formating and order. Expensive operation
     * as the strings are parsed to JsonNodes, which are compared.
     * @param first
     * @param second
     * @return true if first and second are equivalent JSON objects
     * @throws JsonConversionException if unable to convert Json to a JsonNode or if first or second cannot be converted to a JSON string
     */
    public static boolean isJsonEquivalent(Json first, Json second) throws JsonConversionException {
        return isJsonEquivalent(first.asJson(), second.asJson());
    }

    /**
     * Tests whether two json strings are equivalent; ignores formating and order. Expensive operation
     * as the strings are parsed to JsonNodes, which are compared.
     * @param first
     * @param second
     * @return true if first and second are equivalent JSON objects
     * @throws JsonConversionException if unable to convert first or second to JsonNodes
     */
    public static boolean isJsonEquivalent(String first, String second) throws JsonConversionException {
        if(first == null)
            return second == null;
        if(second == null)
            return false;
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
