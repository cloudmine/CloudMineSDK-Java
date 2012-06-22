package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
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
 * A binary file that can be stored in CloudMine. Consists of a file name, content type, and the file
 * contents as bytes. <BR>
 * The JSON representation of a CMFile consists of the CMType (file) and content type (MIME type, defaults to application/octet-stream)
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMFile implements Json {

    /**
     * Instantiate a new CMFile with the given contents, name, and type
     * @param contents the contents of the file
     * @param fileName the name of the file; used to retrieve a stored file. If null, a unique name is generated
     * @param contentType the MIME type. If null, {@link #DEFAULT_CONTENT_TYPE} is used
     * @return a new CMFile
     * @throws CreationException If unable to read in the content stream
     */
    public static CMFile CMFile(InputStream contents, String fileName, String contentType) throws CreationException {
        return new CMFile(contents, fileName, contentType);
    }

    public static class Constructor implements ResponseConstructor<CMFile> {
        private final String key;
        public Constructor(String key) {
            super();
            this.key = key;
        }

        @Override
        public CMFile construct(HttpResponse response) throws CreationException {
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

    /**
     * Check whether the file is null and has any contents
     * @param file the file to check
     * @return true if the file is empty, false if the file has contents
     */
    public static boolean isEmpty(CMFile file) {
        return file == null ||
                (file.getFileContents().length == 1 && file.getFileContents()[0] == 32);
    }

    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String IMAGE_PNG_CONTENT_TYPE = "image/png";
    private static final Logger LOG = LoggerFactory.getLogger(CMFile.class);

    private final String fileName;
    private final String contentType;
    private final byte[] fileContents;

    /**
     * Instantiate a new CMFile with the given contents, name, and type
     * @param fileContents the contents of the file
     * @param fileName the name of the file; used to retrieve a stored file. If null, a unique name is generated
     * @param contentType the MIME type. If null, {@link #DEFAULT_CONTENT_TYPE} is used
     * @return a new CMFile
     * @throws CreationException If given null fileContents
     */
    public static CMFile CMFile(byte[] fileContents, String fileName, String contentType) throws CreationException {
        return new CMFile(fileContents, fileName, contentType);
    }

    /**
     * Instantiate a new CMFile with the given contents. A unique name will be generated, and the DEFAULT_CONTENT_TYPE will be used
     * @param contents the contents of the file
     * @return a new CMFile
     * @throws CreationException If unable to read in the content stream
     */
    public static CMFile CMFile(InputStream contents) throws CreationException {
        return new CMFile(contents);
    }

    /**
     * Instantiate a new CMFile with the given contents, name, and type
     * @param contents the contents of the file
     * @param contentType the MIME type. If null, {@link #DEFAULT_CONTENT_TYPE} is used
     * @return a new CMFile
     * @throws CreationException If given null contents or unable to read in contents
     */
    public static CMFile CMFile(InputStream contents, String contentType) throws CreationException {
        return new CMFile(contents, contentType);
    }

    /**
     * Instantiate a new CMFile, using the entity contents of the HttpResponse. The file will have the
     * given fileName
     * @param response received in response to a loadFile request
     * @param fileName the name of the file. If null a unique name will be generated
     * @return a new CMFile
     * @throws CreationException if unable to read in the entity contents
     */
    public static CMFile CMFile(HttpResponse response, String fileName) throws CreationException {
        return new CMFile(response, fileName);
    }

    CMFile(byte[] fileContents, String fileName, String contentType) throws CreationException {
        if(fileContents == null) {
            throw new CreationException(new NullPointerException("Cannot create a new file with null contents"));
        }
        this.fileName = fileName == null ?
                SimpleCMObject.generateUniqueObjectId() :
                fileName;
        this.contentType = contentType == null ?
                DEFAULT_CONTENT_TYPE :
                contentType;
        this.fileContents = fileContents;
    }

    private CMFile(InputStream contents) throws CreationException {
        this(contents, null, null);
    }
    private CMFile(InputStream contents, String contentType) throws CreationException {
        this(contents, contentType, null);
    }
    private CMFile(HttpResponse response, String fileName) throws CreationException {
        this(extractInputStream(response), fileName, extractContentType(response));
    }

    private CMFile(InputStream contents, String fileName, String contentType) throws CreationException {
        this(inputStreamToByteArray(contents), fileName, contentType);

    }

    private static byte[] inputStreamToByteArray(InputStream contents) throws CreationException {
        try {
            return IOUtils.toByteArray(contents);
        } catch (IOException e) {
            LOG.error("IOException converting file contents to byte array", e);
            throw new CreationException(e);
        }
    }

    /**
     * Get the byte contents of the file
     * @return the byte contents of the file
     */
    public byte[] getFileContents() {
        return fileContents;
    }

    /**
     * Get the contents of the file as an InputStream
     * @return the contents of the file as an InputStream
     */
    public InputStream getFileContentStream() {
        return new ByteArrayInputStream(fileContents);
    }

    /**
     * The name of the file
     * @return the name of the file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * The MIME type for this file
     * @return The MIME type for this file
     */
    public String getContentType() {
        return contentType;
    }

    private static String extractContentType(HttpResponse response) {
        if(response == null || response.getEntity() == null || response.getEntity().getContentType() == null)
            return null;
        return response.getEntity().getContentType().getValue();
    }

    private static InputStream extractInputStream(HttpResponse response) throws CreationException {
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
        hash = hash * 31 + fileName.hashCode();
        hash = hash * 31 + Arrays.hashCode(fileContents);
        return hash;
    }

    @Override
    public final boolean equals(Object other) {
        if(other instanceof CMFile) {
            CMFile otherFile = (CMFile)other;
            return otherFile.getContentType().equals(getContentType()) &&
                    otherFile.getFileName().equals(getFileName()) &&
                    Arrays.equals(otherFile.getFileContents(), getFileContents());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("Key: ").append(fileName).append(" Content-Type: ").append(contentType).append(" contents: ");
        for(int i = 0; i < fileContents.length; i++) {
            string.append(fileContents[i]);
        }
        return string.toString();
    }


    @Override
    public String asJson() throws JsonConversionException {
        return
             JsonUtilities.jsonCollection(
                JsonUtilities.createJsonProperty("key", fileName),
                JsonUtilities.createJsonProperty("content_type", contentType),
                JsonUtilities.createJsonProperty(JsonUtilities.TYPE_KEY, CMType.FILE.asJson())).asJson();

    }
}
