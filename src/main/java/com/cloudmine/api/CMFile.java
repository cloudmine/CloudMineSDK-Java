package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.response.CMResponse;
import com.cloudmine.api.rest.response.ResponseConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Future;

/**
 * The JSON representation of a CMFile consists of the
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/18/12, 3:29 PM
 */
public class CMFile implements Json {

    public static CMFile CMFile(InputStream contents, String key, String contentType) {
        return new CMFile(contents, key, contentType);
    }

    public static class Constructor implements ResponseConstructor<CMFile> {
        private final String key;
        public Constructor(String key) {
            super();
            this.key = key;
        }

        @Override
        public CMFile construct(HttpResponse response) {
            return CMFile(response, key);
        }

        @Override
        public Future<CMFile> constructFuture(Future<HttpResponse> futureResponse) {
            return CMResponse.createFutureResponse(futureResponse, this);
        }
    };

    public static ResponseConstructor<CMFile> constructor(String key) {
        return new Constructor(key);
    }

    public static boolean isEmpty(CMFile file) {
        return file == null ||
                (file.fileContents().length == 1 && file.fileContents()[0] == 32);
    }

    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String IMAGE_PNG_CONTENT_TYPE = "image/png";
    private static final Logger LOG = LoggerFactory.getLogger(CMFile.class);

    private final String key;
    private final String contentType;
    private final byte[] fileContents;

    public static CMFile CMFile(byte[] fileContents, String key, String contentType) {
        return new CMFile(fileContents, key, contentType);
    }

    public static CMFile CMFile(InputStream contents) {
        return new CMFile(contents);
    }

    public static CMFile CMFile(InputStream contents, String contentType) {
        return new CMFile(contents, contentType);
    }

    public static CMFile CMFile(HttpResponse response, String key) {
        return new CMFile(response, key);
    }

    CMFile(byte[] fileContents, String key, String contentType) {
        if(fileContents == null) {
            throw new CreationException(new NullPointerException("Cannot create a new file with null contents"));
        }
        this.key = key == null ?
                SimpleCMObject.generateUniqueKey() :
                key;
        this.contentType = contentType == null ?
                DEFAULT_CONTENT_TYPE :
                contentType;
        this.fileContents = fileContents;
    }

    private CMFile(InputStream contents) {
        this(contents, null, null);
    }
    private CMFile(InputStream contents, String contentType) {
        this(contents, contentType, null);
    }
    private CMFile(HttpResponse response, String key) {
        this(extractInputStream(response), key, extractContentType(response));
    }

    private CMFile(InputStream contents, String key, String contentType) {
        this(inputStreamToByteArray(contents), key, contentType);

    }

    private static byte[] inputStreamToByteArray(InputStream contents) throws CreationException {
        try {
            return IOUtils.toByteArray(contents);
        } catch (IOException e) {
            LOG.error("IOException converting file contents to byte array", e);
            throw new CreationException(e);
        }
    }

    public byte[] fileContents() {
        return fileContents;
    }


    public InputStream getFileContentStream() {
        return new ByteArrayInputStream(fileContents);
    }

    public String key() {
        return key;
    }

    public String contentType() {
        return contentType;
    }

    private static String extractContentType(HttpResponse response) {
        if(response == null || response.getEntity() == null || response.getEntity().getContentType() == null)
            return null;
        return response.getEntity().getContentType().getValue();
    }

    private static InputStream extractInputStream(HttpResponse response) {
        if(response == null || response.getEntity() == null)
            return null;
        try {
            return new BufferedHttpEntity(response.getEntity()).getContent();
        } catch (IOException e) {
            LOG.error("IOException getting response entity contents", e);
            throw new CreationException(e);
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + contentType.hashCode();
        hash = hash * 31 + key.hashCode();
        hash = hash * 31 + Arrays.hashCode(fileContents);
        return hash;
    }

    @Override
    public final boolean equals(Object other) {
        if(other instanceof CMFile) {
            CMFile otherFile = (CMFile)other;
            return otherFile.contentType().equals(contentType()) &&
                    otherFile.key().equals(key()) &&
                    Arrays.equals(otherFile.fileContents(), fileContents());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("Key: ").append(key).append(" Content-Type: ").append(contentType).append(" contents: ");
        for(int i = 0; i < fileContents.length; i++) {
            string.append(fileContents[i]);
        }
        return string.toString();
    }


    @Override
    public String asJson() {
        return
             JsonUtilities.jsonCollection(
                JsonUtilities.createJsonProperty("key", key),
                JsonUtilities.createJsonProperty("content_type", contentType),
                JsonUtilities.createJsonProperty(JsonUtilities.TYPE_KEY, CMType.FILE.asJson())).asJson();

    }
}
