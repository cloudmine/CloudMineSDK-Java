package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.persistance.CMJacksonModule;
import com.cloudmine.api.persistance.CMUserConstructorMixIn;
import com.cloudmine.api.persistance.ClassNameRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Simplify working with JSON by putting all the utility methods in one place. Mostly focused on converting
 * objects to and from JSON
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class JsonUtilities {

    private static final Logger LOG = LoggerFactory.getLogger(JsonUtilities.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static final String EMPTY_JSON = "{ }";

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
        if(JavaCMUser.class.isAssignableFrom(klass)) {
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

    public static String createJsonProperty(String key, boolean value) {
        return new StringBuilder(addQuotes(key)).append(":").append(value).toString();
    }

    public static String createJsonPropertyToJson(String key, String value) {
        return new StringBuilder(addQuotes(key)).append(":").append(value).toString();
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


    public static String getIdentifierBody(String email, String userName) {
        StringBuilder jsonBuilder = new StringBuilder("{");
        String separator = "";
        if(Strings.isNotEmpty(email)) {
            jsonBuilder.append("\"email\": \"").append(email).append("\"");
            separator = ", ";
        }
        if(Strings.isNotEmpty(userName)) {
            jsonBuilder.append(separator).append("\"username\": \"").append(userName).append("\"");
        }
        return jsonBuilder.append("}").toString();
    }

    public static Transportable keyedJsonCollection(Collection<CMObject> cmObjects) {
        if(cmObjects == null || cmObjects.isEmpty()) return new TransportableString(EMPTY_JSON);
        String[] unwrapped = new String[cmObjects.size()];
        int i = 0;
        for(CMObject object : cmObjects) {
            unwrapped[i] = object.asKeyedObject();
            i++;
        }
        return jsonCollection(unwrapped);
    }

    /**
     * Enclose all the passed in jsonEntities in a JSON collection
     * @param jsonEntities to put into the collection
     * @return { jsonEntities[0].transportableRepresentation, jsonEntities[1].transportableRepresentation, ...}
     */
    public static Transportable jsonCollection(Collection<? extends Transportable> jsonEntities) {
        return jsonCollection(jsonEntities.toArray(new Transportable[jsonEntities.size()]));
    }

    public static Transportable jsonCollection(List<String> jsonEntities) {
        return jsonCollection(jsonEntities.toArray(new String[jsonEntities.size()]));
    }

    /**
     * Enclose all the passed in strings in a JSON collection
     * @param jsonEntities JSON strings to put in the collection
     * @return { jsonEntities[0], jsonEntities[1], ...}
     */
    public static Transportable jsonStringsCollection(Collection<String> jsonEntities) {
        return jsonCollection(jsonEntities.toArray(new String[jsonEntities.size()]));
    }

    /**
     * Enclose all the passed in transportableEntities in a JSON collection
     * @param transportableEntities to put into the collection
     * @return { transportableEntities[0].transportableRepresentation, transportableEntities[1].transportableRepresentation, ...}
     */
    public static Transportable jsonCollection(Transportable... transportableEntities) {
        String[] jsonStrings = new String[transportableEntities.length];
        for(int i = 0; i < transportableEntities.length; i++) {
            jsonStrings[i] = transportableEntities[i].transportableRepresentation();
        }
        return jsonCollection(jsonStrings);
    }

    /**
     * Enclose all the passed in strings in a JSON collection
     * @param jsonEntities JSON strings to put in the collection
     * @return { jsonEntities[0], jsonEntities[1], ...}
     */
    public static Transportable jsonCollection(String... jsonEntities) {
        StringBuilder json = new StringBuilder("{\n");
        String comma = "";
        for(String jsonEntity : jsonEntities) {
            json.append(comma)
                    .append(TAB)
                    .append(jsonEntity);
            comma = ",\n";
        }
        json.append("\n}");
        return new TransportableString(json.toString());
    }

    /**
     * Convert a Map to its representation as a JSON string.
     * @param map will be converted to its JSON representation
     * @return valid JSON that represents the passed in map. It should be true that map.equals(jsonToMap(mapToJson(map)))
     * @throws ConversionException if unable to convert this Map to json. This should never happen
     */
    public static String mapToJson(Map<String, ? extends Object> map) throws ConversionException {
        if(map == null) {
            return EMPTY_JSON;
        }
        StringWriter writer = new StringWriter();
        try {
            jsonMapper.writeValue(writer, map);
            return writer.toString();
        } catch (IOException e) {
            LOG.error("Trouble writing json", e);
            throw new ConversionException(e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //nope don't care
            }
        }
    }

    public static <CMOBJECT extends CMObject> String cmobjectsToJson(Collection<CMOBJECT> objects) throws ConversionException {
        if(objects == null || objects.size() == 0) {
            return EMPTY_JSON;
        }
        return cmobjectsToJson(objects.toArray(new CMObject[objects.size()]));
    }

    /**
     * Convert a CMObject to its JSON representation
     * @param objects the objects to convert
     * @return valid JSON that represents the passed in objects as a collection of JSON
     * @throws ConversionException if unable to convert this CMObject to json
     */
    public static String cmobjectsToJson(CMObject... objects) throws ConversionException {
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
            throw new ConversionException(e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                //nope don't care
            }
        }
    }

    public static String cmObjectToJson(Object object) {
        try {
            return wrap(jsonMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            throw new ConversionException(e);
        }
    }

    public static String objectToJson(Object object) throws ConversionException {
        StringWriter writer = new StringWriter();
        try {
            jsonMapper.writeValue(writer, object);
            return writer.toString();
        } catch (IOException e) {
            LOG.error("Exception thrown", e);
            throw new ConversionException(e);
        }
    }

    public static void writeObjectToJson(Object object, OutputStream stream) {
        try {
            jsonMapper.writeValue(stream, object);
        } catch (IOException e) {
            LOG.error("Exception thrown", e);
            throw new ConversionException(e);
        }
    }

    /**
     * Convert the given JSON to the given klass. If unable to convert, throws ConversionException
     * @param json JSON representing
     * @param klass
     * @param <CMO>
     * @return
     * @throws ConversionException
     */
    public static <CMO> CMO jsonToClass(String json, Class<CMO> klass) throws ConversionException {
        try {
            CMO object = jsonMapper.readValue(json, klass);
            return object;
        }catch (IOException e) {
            LOG.error("Trouble reading json: \n" + json, e);
            throw new ConversionException("JSON: " + json, e);
        }
    }

    public static CMObject jsonToClass(String json) throws ConversionException {
        if(Strings.isEmpty(json)) {
            throw new ConversionException("Can't convert an empty or null json string");
        }
        Map<String, Object> jsonMap = jsonToMap(json); //this is a slow but easy way to get the klass name, might have to be replaced in the future
        Object klassString = jsonMap.get(CLASS_KEY);
        CMType type = CMType.getTypeById(Strings.asString(jsonMap.get(TYPE_KEY)));

        boolean isTyped = type != null &&
                                !CMType.NONE.equals(type) &&
                                klassString == null; //if we have a class string, use that instead of the specified type
        if(isTyped) {
            return jsonToClass(json, type.getTypeClass());
        }

        boolean isUnknownClass = klassString == null ||
                ClassNameRegistry.isRegistered(klassString.toString()) == false;
        if(isUnknownClass) {
            return new SimpleCMObject(new TransportableString(json));
        }

        Class<? extends CMObject> klass = ClassNameRegistry.forName(klassString.toString());
        return jsonToClass(json, klass);
    }

    /**
     * Convert a Transportable entity to a Map representation
     * @param transportable valid JSON
     * @return If transportable is null, returns an empty Map. Otherwise, a Map whose keys are JSON Strings and whose values are JSON values
     * @throws ConversionException if unable to convert the given transportable to a map. Will happen if the transportableRepresentation call fails or if unable to represent the transportable as a map
     */
    public static Map<String, Object> jsonToMap(Transportable transportable) throws ConversionException {
        if(transportable == null)
            return new HashMap<String, Object>();
        return jsonToMap(transportable.transportableRepresentation());
    }

    /**
     * Convert a JSON string to a Map representation
     * @param json valid JSON
     * @return If json is null, returns an empty Map. Otherwise, a Map whose keys are JSON Strings and whose values are JSON values
     * @throws ConversionException if unable to convert the given json to a map. Will happen if the transportableRepresentation call fails or if unable to represent the json as a map
     */
    public static Map<String, Object> jsonToMap(String json) throws ConversionException {
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
     * @throws ConversionException
     */
    public static <CMO> Map<String, CMO> jsonToClassMap(String json, Class<CMO> klass) throws ConversionException {
        try {
            MapType mapType = jsonMapper.getTypeFactory().constructMapType(Map.class, String.class, klass);
            Map<String, CMO> jsonMap = jsonMapper.readValue(json, mapType);
            return jsonMap;
        } catch (IOException e) {
            LOG.error("Trouble reading json", e);
            throw new ConversionException("JSON: " + json, e);
        }
    }

    public static Map<String, CMObject> jsonToClassMap(String json) {
        Map<String, String> simpleMap = jsonMapToKeyMap(json);
        Map<String, CMObject> objectMap = new LinkedHashMap<String, CMObject>();
        for(Map.Entry<String, String> entry : simpleMap.entrySet()) {
            String objectId = entry.getKey();
            CMObject cmObject = jsonToClass(entry.getValue());
            cmObject.setObjectId(objectId);
            objectMap.put(objectId, cmObject);
        }
        return objectMap;
    }

    public static <CMO extends CMObject> Map<String, CMO> jsonToCMObjectMap(String json, Class<CMO> klass) {

        Map<String, String> simpleMap = jsonMapToKeyMap(json);
        Map<String, CMO> objectMap = new LinkedHashMap<String, CMO>();
        for(Map.Entry<String, String> entry : simpleMap.entrySet()) {
            objectMap.put(entry.getKey(), jsonToClass(entry.getValue(), klass));
        }
        return objectMap;
    }

    /**
     * This method only works if all the values are objects, otherwise it breaks
     * @param json
     * @return
     */
    public static Map<String, String> jsonMapToKeyMap(String json) {
        //TODO this method is big and kinda gross
        //TODO Also its kind of broken on input that has top level keys to none json objects
        if(Strings.isEmpty(json)) {
            return new HashMap<String, String>();
        }
        try {
            StringReader reader = new StringReader(json);
            int readInt;
            int open = 0;
            boolean inString = false;
            boolean escapeNext = false;
            Map<String, String> jsonMap = new LinkedHashMap<String, String>();
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
                                    throw new ConversionException("Missing key at: " + key);
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
                        if(escapeNext) {
                            escapeNext = false;
                        } else {
                            inString = !inString;
                        }
                        break;
                    case '\\':
                        escapeNext = true;
                        break;
                    default:
                        escapeNext = false; //only escape the next character

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
            throw new ConversionException("Trouble reading JSON: " + e);
        }
    }

    public static void mergeJsonUpdates(CMObject objectToUpdate, String json) throws ConversionException {
        try {
            jsonMapper.readerForUpdating(objectToUpdate).readValue(json);
        } catch (IOException e) {
            LOG.error("Exception thrown while merging json update: " + json, e);
            throw new ConversionException(e);
        }
    }

    /**
     * Replaces any json datetime objects with dates. Modifies the passed in map
     * @param jsonMap
     * @return
     * @throws ConversionException
     */
    private static Object convertDateClassesToDates(Map<String, Object> jsonMap) throws ConversionException {
        if(jsonMap == null)
            return null;
        boolean isDateClass = jsonMap.containsKey(CLASS_KEY) &&
                DATE_CLASS.equals(jsonMap.get(CLASS_KEY));
        if(isDateClass) {
            Object time = jsonMap.get(TIME_KEY);
            if(time instanceof Number) {
                return CMDateFormat.fromNumber((Number) time); //replace Number with Date
            } else {
                throw new ConversionException("Received non number time");
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
     * @throws ConversionException if unable to convert the given json to a map. Will happen if the transportableRepresentation call fails or if unable to represent the json as a map
     */
    public static Map<String, Object> jsonToMap(InputStream inputJson) throws ConversionException {
        if(inputJson == null) {
            return new HashMap<String, Object>();
        }
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(inputJson, writer, ENCODING);
            return jsonToMap(writer.toString());
        } catch (IOException e) {
            throw new ConversionException("Couldn't read inputJson", e);
        }
    }

    /**
     * Tests whether two json strings are equivalent; ignores formating and order. Expensive operation
     * as the strings are parsed to JsonNodes, which are compared.
     * @param first
     * @param second
     * @return true if first and second are equivalent JSON objects
     * @throws ConversionException if unable to convert Transportable to a JsonNode or if first or second cannot be converted to a JSON string
     */
    public static boolean isJsonEquivalent(Transportable first, Transportable second) throws ConversionException {
        return isJsonEquivalent(first.transportableRepresentation(), second.transportableRepresentation());
    }

    /**
     * Tests whether two json strings are equivalent; ignores formating and order. Expensive operation
     * as the strings are parsed to JsonNodes, which are compared.
     * @param first
     * @param second
     * @return true if first and second are equivalent JSON objects
     * @throws ConversionException if unable to convert first or second to JsonNodes
     */
    public static boolean isJsonEquivalent(String first, String second) throws ConversionException {
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
                throw new ConversionException("Couldn't convert second string to json: " + second, e);
            }
        } catch (IOException e) {
            throw new ConversionException("Couldn't convert first string to json: " + first, e);
        }
    }
}
