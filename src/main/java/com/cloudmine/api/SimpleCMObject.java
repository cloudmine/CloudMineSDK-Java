package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonString;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.ObjectModificationResponse;

import java.util.*;
import java.util.concurrent.Future;

/**
 * An object that can be inserted, updated, deleted, and retrieved from CloudMine. Has a non optional
 * object id that is used to uniquely identify the object; if one is not provided at creation time, a
 * random id is generated. May be associated with a {@link CMStore} through the use of a {@link StoreIdentifier},
 * which allows the object to be modified on the CloudMine platform.
 * Values can be added to the object through the use of the add method. In general, values will be converted
 * to JSON based on the rules defined in the <a href="www.json.org">JSON specification.</a> The special cases are
 * as follows:
 * Map<String, Object> are treated as JSON objects
 * Dates are converted into JSON objects
 * CloudMine specific types ({@link CMGeoPoint} and {@link CMFile}) are converted to JSON objects
 * SimpleCMObjects have 2 optional properties: class and type. Class is used for loading all similar objects, for
 * example through {@link CMStore#loadUserObjectsOfClass(String)}
 * Type is reserved for CloudMine specific types, such as geopoints or files. It is unlikely you will need
 * to set this type yourself
 *
 * Copyright CloudMine LLC
 */
public class SimpleCMObject implements Json {

    private final Map<String, Object> contents;
    private final Map<String, Object> topLevelMap;
    private final String objectId;
    private Immutable<StoreIdentifier> storeId = new Immutable<StoreIdentifier>();


    public static SimpleCMObject SimpleCMObject() throws CreationException {
        return new SimpleCMObject();
    }

    public static SimpleCMObject SimpleCMObject(String topLevelKey) throws CreationException {
        return new SimpleCMObject(topLevelKey);
    }

    public static SimpleCMObject SimpleCMObject(Json json) throws CreationException {
        try {
            return new SimpleCMObject(json);
        } catch (JsonConversionException e) {
            throw new CreationException(e);
        }
    }

    public static SimpleCMObject SimpleCMObject(final String topLevelKey, final Map<String, Object> contents) throws CreationException {
        return new SimpleCMObject(topLevelKey, contents);
    }

    public static SimpleCMObject SimpleCMObject(Map<String, Object> objectMap) throws CreationException {
        return new SimpleCMObject(objectMap);
    }

    SimpleCMObject() throws CreationException {
        this(generateUniqueKey());
    }

    SimpleCMObject(String objectId) throws CreationException {
        this(objectId, new HashMap<String, Object>());
    }

    SimpleCMObject(Json json) throws JsonConversionException, CreationException {
        this(JsonUtilities.jsonToMap(json));
    }

    SimpleCMObject(final String objectId, final Map<String, Object> contents) throws CreationException {
        this(new HashMap<String, Object>() {
            {
                put(objectId, contents);
            }
        });
    }

    /**
     * Creates a SimpleCMObject from a Map. If the map has only one entry, it is assumed to be a top
     * level key mapped to the contents of the object, unless that single entry is not a Map<String, Object>.
     * If the objectMap has more than one key or consists of a single key mapped to a non string keyed map value,
     * a top level key is generated and the objectMap is assumed to be the contents.
     *
     * @param objectMap
     */
    SimpleCMObject(Map<String, Object> objectMap) throws CreationException {

        if(objectMap.size() != 1 ||
                isMappedToAnotherMap(objectMap) == false) {
            objectId = generateUniqueKey();
            contents = objectMap;
            topLevelMap = new HashMap<String, Object>();
            topLevelMap.put(objectId, contents);
        } else {
            Set<Map.Entry<String, Object>> contentSet = objectMap.entrySet();
            this.topLevelMap = objectMap;
            Map.Entry<String, Object> contentsEntry = contentSet.iterator().next();
            objectId = contentsEntry.getKey();

            try {
                contents = (Map<String, Object>)contentsEntry.getValue();
            } catch(ClassCastException e) {
                throw new CreationException("Passed a topLevelMap that does not contain a Map<String, Object>", e);
            }
        }
    }

    /**
     * Set what store to save this object with. If this is not set, it is assumed to be saved with
     * the default, app level store. If you are saving the object to a user level store, the user must
     * be logged in when save is called. Once the StoreIdentifier is set, it cannot be changed.
     * @param identifier the identifier for the store this should be saved with. calling setSaveWith(null)
     *                   is the same as not calling setSaveWith, and false will be returned
     * @return true if the value was set; false if it has already been set OR null was passed in
     */
    public boolean setSaveWith(StoreIdentifier identifier) {
        return storeId.setValue(identifier);
    }

    /**
     * Set that this object should be saved at the User level, and should be saved using the given CMSessionToken.
     * @param session the session of the user to save this SimpleCMObject with
     * @return true if the value was set; false if it has already been set OR null was passed in
     * @throws CreationException if session is null
     */
    public boolean setSaveWith(CMSessionToken session) throws CreationException {
        return setSaveWith(new StoreIdentifier(session));
    }

    public StoreIdentifier getSavedWith() {
        return storeId.value(StoreIdentifier.DEFAULT);
    }

    public boolean isOnLevel(ObjectLevel level) {
        return getSavedWith().isLevel(level);
    }

    /**
     * Asynchronously save this object to its store. If no store has been set, it saves to the app
     * level store.
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert this object to JSON; should not happen
     * @throws CreationException if {@link CMApiCredentials#initialize(String, String)} has not been called
     */
    public Future<ObjectModificationResponse> save() throws JsonConversionException, CreationException {
        return save(Callback.DO_NOTHING);
    }

    /**
     * Asynchronously save this object to its store. If no store has been set, it saves to the app
     * level store.
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert this object to JSON; should not happen
     * @throws CreationException if {@link CMApiCredentials#initialize(String, String)} has not been called
     */
    public Future<ObjectModificationResponse> save(Callback callback) throws CreationException, JsonConversionException {
        return store().saveObject(this, callback);
    }

    private CMStore store() throws CreationException {
        return CMStore.getStore(storeId.value(StoreIdentifier.DEFAULT));
    }

    protected static String generateUniqueKey() {
        return UUID.randomUUID().toString();
    }

    private static boolean isMappedToAnotherMap(Map<String, Object> objectMap) {
        if(objectMap.size() == 1) {
            Object mapValue = objectMap.values().iterator().next();
            if(mapValue instanceof Map) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value associated with the top level key; This will probably be a Map of Strings to
     * Objects, but may just be a single value
     * @return
     */
    public Object getValue() {
        return topLevelMap.get(objectId);
    }

    public Object get(String key) {
        return contents.get(key);
    }

    public SimpleCMObject getSimpleCMObject(String key) throws JsonConversionException {
        return getValue(key, SimpleCMObject.class);
    }

    public SimpleCMObject getSimpleCMObject(String key, SimpleCMObject alternative) throws JsonConversionException {
        SimpleCMObject value = getSimpleCMObject(key);
        return value == null ?
                alternative :
                    value;
    }

    public <T> List<T> getList(String key) throws JsonConversionException {
        return getValue(key, List.class);
    }

    public Integer getInteger(String key, Integer alternative) throws JsonConversionException {
        Integer value = getInteger(key);
        return value == null ?
                alternative :
                    value;
    }

    public Integer getInteger(String key) throws JsonConversionException {
        return getValue(key, Integer.class);
    }

    public Double getDouble(String key) throws JsonConversionException {
        return getValue(key, Double.class);
    }

    public Double getDouble(String... keys) throws JsonConversionException {
        for(String key : keys) {
            Double val = getDouble(key);
            if(val != null) {
                return val;
            }
        }
        return null;
    }

    public Double getDouble(String key, Double alternative) throws JsonConversionException {
        Double value = getDouble(key);
        return value == null ?
                alternative :
                    value;
    }

    public Boolean getBoolean(String key, Boolean alternative) throws JsonConversionException {
        Boolean value = getBoolean(key);
        return value == null ?
                alternative :
                    value;
    }

    /**
     * Returns Boolean.TRUE, Boolean.FALSE, or null if the key does not exist
     * @param key
     * @return
     */
    public Boolean getBoolean(String key) throws JsonConversionException {
        return getValue(key, Boolean.class);
    }

    public String getString(String key, String alternative) throws JsonConversionException {
        String string = getString(key);
        return string == null ?
                alternative :
                    string;
    }

    public String getString(String key) throws JsonConversionException {
        return getValue(key, String.class);
    }

    public Date getDate(String key, Date alternative) throws JsonConversionException {
        Date date = getDate(key);
        return date == null ?
                alternative :
                    date;
    }

    public Date getDate(String key) throws JsonConversionException {
        return getValue(key, Date.class);
    }

    public CMGeoPoint getGeoPoint(String key) throws JsonConversionException {
        return getValue(key, CMGeoPoint.class);
    }

    public CMGeoPoint getGeoPoint(String key, CMGeoPoint alternative) throws JsonConversionException {
        CMGeoPoint point = getGeoPoint(key);
        return point == null ?
                alternative :
                    point;
    }

    /**
     * Returns null if the key or klass are null, if the value linked to the specified key is not convertable to
     * the given klass,
     * @param key
     * @param klass
     * @param <T>
     * @return
     */
    private <T> T getValue(String key, Class<T> klass) throws JsonConversionException {
        Object value = contents.get(key);
        if(key == null || klass == null || value == null) {
            return null;
        }
        Class<? extends Object> valueClass = value.getClass();
        if(value != null &&
                SimpleCMObject.class.isAssignableFrom(klass)) {
            boolean isMap = Map.class.isAssignableFrom(valueClass);
            boolean isJson = Json.class.isAssignableFrom(valueClass);
            String valueString = isMap ?
                                    JsonUtilities.mapToJson((Map)value) :
                                        isJson ?
                                                ((Json)value).asJson() :
                                                    value.toString();

            try {
                return newFromJson(valueString, klass);
            } catch (CreationException e) {
                throw new JsonConversionException(e);
            }
        }
        if(value != null && klass.isAssignableFrom(valueClass)) {
            return (T)value;
        }
        return null;
    }

    /**
     * Returns null if json is null, klass is null, or klass isn't assignable from SimpleCMObject
     * @param json
     * @param klass
     * @param <T>
     * @return
     */
    private <T> T newFromJson(String json, Class<T> klass) throws CreationException {
        if(json == null || klass == null)
            return null;
        //Need to start at the bottom of the inheritance chain and work up
        if(CMGeoPoint.class.isAssignableFrom(klass)) {
            return (T) CMGeoPoint.CMGeoPoint(new JsonString(json));
        } else if(SimpleCMObject.class.isAssignableFrom(klass)) {
            return (T) SimpleCMObject(new JsonString(json));
        }
        return null;
    }

    public void setClass(String className) {
        add(JsonUtilities.CLASS_KEY, className);
    }

    public void setType(CMType type) {
        add(JsonUtilities.TYPE_KEY, type.getTypeId());
    }

    public CMType getType() {
        Object type = get(JsonUtilities.TYPE_KEY);
        return CMType.getTypeById((String) type);
    }

    public boolean isType(CMType type) {
        return getType().equals(type);
    }

    public SimpleCMObject add(String key, Object value) {
        contents.put(key, value);
        return this;
    }

    public SimpleCMObject add(SimpleCMObject value) {
        add(value.getObjectId(), value);
        return this;
    }

    /**
     * Removes the object with the given key.
     * @param key of the object to remove
     * @return the removed object if it exists
     */
    public final Object remove(String key) {
        return contents.remove(key);
    }

    public String getObjectId() {
        return objectId;
    }

    public String asKeyedObject() throws JsonConversionException {
        return JsonUtilities.addQuotes(objectId) + ":" + asUnkeyedObject();
    }

    public String asUnkeyedObject() throws JsonConversionException {
        return JsonUtilities.mapToJson(contents);
    }

    public String asJson() throws JsonConversionException {
        return JsonUtilities.mapToJson(topLevelMap);
    }

    public String toString() {
        try {
            return asJson();
        } catch (JsonConversionException e) {
            return "Invalid json: " + e.getMessage();
        }
    }

    /**
     * Checks whether the internal map that represents this SimpleCMObject is the same as the passed in Map.
     * Uses this object's map.equals method
     * @param topLevelMap
     * @return true if the maps are equal, false otherwise
     */
    public boolean isSameMap(Map<String, Object> topLevelMap) {
        return this.topLevelMap.equals(topLevelMap);
    }

    @Override
    public final boolean equals(Object another) {
        //This will make it work with subclasses of SimpleCMObject, but must make equals final so child classes
        //can't provide an implementation that would mean a.equals(b) != b.equals(a)
        if(another instanceof SimpleCMObject) {
            return ((SimpleCMObject) another).isSameMap(topLevelMap);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return topLevelMap.hashCode();
    }
}
