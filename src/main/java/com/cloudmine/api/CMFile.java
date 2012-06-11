package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.Json;
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
import java.util.concurrent.Future;

/**
 * The JSON representation of a CMFile consists of the
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/18/12, 3:29 PM
 */
public class CMFile implements Json {


    public static class Constructor implements ResponseConstructor<CMFile> {
        private final String key;
        public Constructor(String key) {
            super();
            this.key = key;
        }

        @Override
        public CMFile construct(HttpResponse response) {
            return new CMFile(response, key);
        }

        @Override
        public Future<CMFile> constructFuture(Future<HttpResponse> futureResponse) {
            return CMResponse.createFutureResponse(futureResponse, this);
        }
    };

    public static ResponseConstructor<CMFile> constructor(String key) {
        return new Constructor(key);
    }

    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String IMAGE_PNG_CONTENT_TYPE = "image/png";
    private static final Logger LOG = LoggerFactory.getLogger(CMFile.class);

    private final String key;
    private final String contentType;
    private final byte[] fileContents;

    public CMFile(byte[] fileContents, String key, String contentType) {
        this.key = key;
        this.contentType = contentType;
        this.fileContents = fileContents;
    }


    public CMFile(InputStream contents) {
        this(contents, null, null);
    }

    public CMFile(InputStream contents, String contentType) {
        this(contents, contentType, null);
    }

    public CMFile(HttpResponse response, String key) {
        this(extractInputStream(response), extractContentType(response), key);
    }

    public CMFile(InputStream contents, String contentType, String key) {
        if(contents == null) {
            throw new CreationException(new NullPointerException("Cannot create a new file with empty contents"));
        }
        this.key = key;
        this.contentType = contentType == null ?
                                    DEFAULT_CONTENT_TYPE :
                                        contentType;
        try {
            fileContents = IOUtils.toByteArray(contents);
        } catch (IOException e) {
            LOG.error("IOException converting file contents to byte array", e);
            throw new CreationException(e);
        }
    }

    public byte[] getFileContents() {
        return fileContents;
    }


    public InputStream getFileContentStream() {
        return new ByteArrayInputStream(fileContents);
    }

    public String getKey() {
        return key;
    }

    public String getContentType() {
        return contentType;
    }

    private static String extractContentType(HttpResponse response) {
        if(response == null || response.getEntity() == null || response.getEntity().getContentType() == null)
            return null;
        return response.getEntity().getContentType().toString();
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
