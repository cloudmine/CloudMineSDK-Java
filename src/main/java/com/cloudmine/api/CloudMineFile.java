package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/18/12, 3:29 PM
 */
public class CloudMineFile {
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final Logger LOG = LoggerFactory.getLogger(CloudMineFile.class);

    private final String key;
    private final String contentType;
    private final byte[] fileContents;

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
            return response.getEntity().getContent();
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

}
