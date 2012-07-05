package com.cloudmine.api.rest;

import com.cloudmine.api.CMFile;
import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.CMType;
import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.response.ResponseBase;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMJacksonModule extends SimpleModule {
    private static final Logger LOG = LoggerFactory.getLogger(CMJacksonModule.class);

    public CMJacksonModule() {
        super("CustomModule", new Version(1, 0, 0, null));
        addSerializer(new JsonSerializer<Date>() {

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
                jgen.writeStartObject();
                jgen.writeRaw(JsonUtilities.convertDateToUnwrappedJsonClass(value));
                jgen.writeEndObject();
            }

            @Override
            public Class<Date> handledType() {
                return Date.class;
            }
        });

        addDeserializer(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                ObjectMapper mapper = (ObjectMapper)jp.getCodec();
                ObjectNode root = (ObjectNode) mapper.readTree(jp);
                JsonNode classNode = root.get(JsonUtilities.CLASS_KEY);
                boolean isDate = classNode != null &&
                            JsonUtilities.DATE_CLASS.equals(classNode.asText());
                if(isDate) {
                    JsonNode timeNode = root.get(JsonUtilities.TIME_KEY);
                    if(timeNode != null) {
                        Long seconds = timeNode.asLong();
                        Date date = new Date(seconds * 1000);
                        return date;
                    }
                }
                return null;
            }
        });

        addSerializer(new JsonSerializer<SimpleCMObject>() {

            @Override
            public void serialize(SimpleCMObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeStartObject();
                String json = null;
                try {
                    json = value.asUnkeyedObject();
                } catch (JsonConversionException e) {
                    LOG.error("Error while serializing, sending empty json", e);
                    json = JsonUtilities.EMPTY_JSON;
                }
                jgen.writeRaw(JsonUtilities.unwrap(json));
                jgen.writeEndObject();
            }

            @Override
            public Class<SimpleCMObject> handledType() {
                return SimpleCMObject.class;
            }
        });

        addSerializer(jsonSerializerForType(CMFile.class));
        addSerializer(jsonSerializerForType(CMSessionToken.class));
        addSerializer(jsonSerializerForType(CMType.class));
        addSerializer(jsonSerializerForType(JsonString.class));
        addSerializer(jsonSerializerForType(ResponseBase.class));
    }

    private static <JSON extends Json> JsonSerializer<JSON> jsonSerializerForType(final Class<JSON> type) {
        return new JsonSerializer<JSON>() {
            @Override
            public void serialize(JSON value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                jgen.writeStartObject();
                String json;
                try {
                    json = value.asJson();
                } catch (JsonConversionException e) {
                    LOG.error("Error while serializing, sending empty json", e);
                    json = JsonUtilities.EMPTY_JSON;
                }
                jgen.writeRaw(JsonUtilities.unwrap(json));
                jgen.writeEndObject();
            }

            @Override
            public Class<JSON> handledType() {
                return type;
            }
        };
    }
}
