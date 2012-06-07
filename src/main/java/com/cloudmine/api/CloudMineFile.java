package com.cloudmine.api;

import android.graphics.Bitmap;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.response.CloudMineResponse;
import com.cloudmine.api.rest.Json;
import com.loopj.android.http.ResponseConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * The JSON representation of a CloudMineFile consists of the
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/18/12, 3:29 PM
 */
public class CloudMineFile implements Json {


    public static class Constructor implements ResponseConstructor<CloudMineFile>{
        private final String key;
        public Constructor(String key) {
            super();
            this.key = key;
        }

        @Override
        public CloudMineFile construct(HttpResponse response) {
            return new CloudMineFile(response, key);
        }

        @Override
        public Future<CloudMineFile> constructFuture(Future<HttpResponse> futureResponse) {
            return CloudMineResponse.createFutureResponse(futureResponse, this);
        }
    };

    public static ResponseConstructor<CloudMineFile> constructor(String key) {
        return new Constructor(key);
    }

    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String IMAGE_PNG_CONTENT_TYPE = "image/png";
    private static final Logger LOG = LoggerFactory.getLogger(CloudMineFile.class);

    private final String key;
    private final String contentType;
    private final byte[] fileContents;

    public CloudMineFile(Bitmap picture) {
        this(picture, null);
    }

    public CloudMineFile(Bitmap picture, String key) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.JPEG, 100, output);

        fileContents = output.toByteArray();
        this.key = key;
        contentType = IMAGE_PNG_CONTENT_TYPE;
    }

    public CloudMineFile(InputStream contents) {
        this(contents, null, null);
    }

    public CloudMineFile(InputStream contents, String contentType) {
        this(contents, contentType, null);
    }

    public CloudMineFile(HttpResponse response, String key) {
        this(extractInputStream(response), extractContentType(response), key);
    }

    public CloudMineFile(InputStream contents, String contentType, String key) {
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
