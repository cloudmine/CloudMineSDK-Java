package com.cloudmine.api;

import android.os.Parcel;
import android.os.Parcelable;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonUtilities;

import java.util.*;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/24/12, 1:29 PM
 */
public class SimpleCMObject implements Json, Parcelable {
    public static final Creator<SimpleCMObject> CREATOR =
            new Creator<SimpleCMObject>() {
                @Override
                public SimpleCMObject createFromParcel(Parcel parcel) {
                    return new SimpleCMObject(parcel);
                }

                @Override
                public SimpleCMObject[] newArray(int i) {
                    return new SimpleCMObject[i];
                }
            };

    private final Map<String, Object> contents;
    private final Map<String, Object> topLevelMap;
    private final String topLevelKey;

    public SimpleCMObject() {
        this(null, new HashMap<String, Object>(), new HashMap<String, Object>());
    }

    public SimpleCMObject(String topLevelKey, Map<String, Object> contents, Map<String, Object> topLevelMap) {
        if(topLevelKey == null)
            topLevelKey = UUID.randomUUID().toString();
        this.topLevelKey = topLevelKey;
        this.contents = contents;
        this.topLevelMap = topLevelMap;
        topLevelMap.put(topLevelKey, contents);
    }

    public SimpleCMObject(String json) {
        this(JsonUtilities.jsonToMap(json));
    }

    public SimpleCMObject(final String topLevelKey, final Map<String, Object> contents) {
        this(new HashMap<String, Object>() {
            {
                put(topLevelKey, contents);
            }
        });
    }

    public SimpleCMObject(Parcel in) {
        this(in.readString());
    }

    public SimpleCMObject(Map<String, Object> topLevelMap) {
        this.topLevelMap = topLevelMap;
        Set<Map.Entry<String, Object>> contentSet = topLevelMap.entrySet();
        if(contentSet.size() != 1) {
            throw new CreationException("Cannot create a CMObject from a map without exactly 1 key. Had: " + contentSet.size());
        }
        Map.Entry<String, Object> contentsEntry = contentSet.iterator().next();
        topLevelKey = contentsEntry.getKey();

        try {
            contents = (Map<String, Object>)contentsEntry.getValue();
        } catch(ClassCastException e) {
            throw new CreationException("Passed a topLevelMap that does not contain a Map<String, Object>", e);
        }
    }

    public Object get(String key) {
        return contents.get(key);
    }

    public Integer getInteger(String key, Integer alternative) {
        Integer value = getInteger(key);
        return value == null ?
                alternative :
                    value;
    }

    public Integer getInteger(String key) {
        return getValue(key, Integer.class);
    }

    public Boolean getBoolean(String key, Boolean alternative) {
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
    public Boolean getBoolean(String key) {
        return getValue(key, Boolean.class);
    }

    public String getString(String key, String alternative) {
        String string = getString(key);
        return string == null ?
                alternative :
                    string;
    }

    public String getString(String key) {
        return getValue(key, String.class);
    }

    public Date getDate(String key, Date alternative) {
        Date date = getDate(key);
        return date == null ?
                alternative :
                    date;
    }

    public Date getDate(String key) {
        return getValue(key, Date.class);
    }

    private <T> T getValue(String key, Class<T> klass) {
        Object value = contents.get(key);
        if(value != null && klass.isAssignableFrom(value.getClass())) {
            return (T)value;
        }
        return null;
    }

    public void setClass(String className) {
        add(JsonUtilities.CLASS_KEY, className);
    }

    public final void add(String key, Object value) {
        if(value instanceof SimpleCMObject) {
            contents.put(key, ((SimpleCMObject)value).asJson());
        }else {
            contents.put(key, value);
        }
    }

    public String asKeyedObject() {
        return JsonUtilities.addQuotes(topLevelKey) + ":" + JsonUtilities.mapToJson(contents);
    }

    public String asJson() {
        return JsonUtilities.mapToJson(topLevelMap);
    }

    public String toString() {
        return asJson();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(asJson());
    }
}
