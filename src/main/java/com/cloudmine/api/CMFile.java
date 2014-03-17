package com.cloudmine.api;

import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.Transportable;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.Savable;
import com.cloudmine.api.rest.callbacks.CMCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.FileCreationResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * A binary file that can be stored in CloudMine. Consists of a file name, content type, and the file
 * contents as bytes. <BR>
 * The transportable representation of a CMFile consists of the CMType (file) and content type (MIME type, defaults to application/octet-stream)
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMFile implements Transportable, Savable<FileCreationResponse, ObjectModificationResponse> {


    /**
     * Check whether the file is null and has any contents
     * @param file the file to check
     * @return true if the file is empty, false if the file has contents
     */
    public static boolean isEmpty(CMFile file) {
        return file == null ||
                (file.getFileContents().length == 1 && file.getFileContents()[0] == 32);
    }

    public static final String TYPE_VALUE = "file";
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String IMAGE_PNG_CONTENT_TYPE = "image/png";
    private static final Logger LOG = LoggerFactory.getLogger(CMFile.class);

    private final String fileId;
    private final String contentType;
    private final byte[] fileContents;
    private Immutable<StoreIdentifier> storeId = new Immutable<StoreIdentifier>();

    /**
     * Instantiate a new CMFile with the given contents, name, and type
     * @param fileContents the contents of the file
     * @param fileId the name of the file; used to retrieve a stored file. If null, a unique name is generated
     * @param contentType the MIME type. If null, {@link #DEFAULT_CONTENT_TYPE} is used
     * @throws CreationException If given null fileContents
     */
    public CMFile(byte[] fileContents, String fileId, String contentType) throws CreationException {
        if(fileContents == null) {
            throw new CreationException(new NullPointerException("Cannot create a new file with null contents"));
        }
        this.fileId = fileId == null ?
                CMObject.generateUniqueObjectId() :
                fileId;
        this.contentType = contentType == null ?
                DEFAULT_CONTENT_TYPE :
                contentType;
        this.fileContents = fileContents;
    }

    /**
     * Instantiate a new CMFile with the given contents. A unique name will be generated, and the DEFAULT_CONTENT_TYPE will be used
     * @param contents the contents of the file
     * @throws CreationException If unable to read in the content stream
     */
    public CMFile(InputStream contents) throws CreationException {
        this(contents, null, null);
    }

    /**
     * Instantiate a new CMFile with the given contents, name, and type
     * @param contents the contents of the file
     * @param contentType the MIME type. If null, {@link #DEFAULT_CONTENT_TYPE} is used
     * @throws CreationException If given null contents or unable to read in contents
     */
    public CMFile(InputStream contents, String contentType) throws CreationException {
        this(contents, null, contentType);
    }

    /**
     * Instantiate a new CMFile, using the entity contents of the HttpResponse. The file will have the
     * given fileId
     * @param response received in response to a loadFile request
     * @param fileId the name of the file. If null a unique name will be generated
     * @throws CreationException if unable to read in the entity contents
     */
    public CMFile(HttpResponse response, String fileId) throws CreationException {
        this(extractInputStream(response), fileId, extractContentType(response));
    }

    /**
     * Instantiate a new CMFile with the given contents, name, and type
     * @param contents the contents of the file
     * @param fileId the name of the file; used to retrieve a stored file. If null, a unique name is generated
     * @param contentType the MIME type. If null, {@link #DEFAULT_CONTENT_TYPE} is used
     * @throws CreationException If unable to read in the content stream
     */
    public CMFile(InputStream contents, String fileId, String contentType) throws CreationException {
        this(inputStreamToByteArray(contents), fileId, contentType);

    }

    private static byte[] inputStreamToByteArray(InputStream contents) throws CreationException {
        try {
            return IOUtils.toByteArray(contents);
        } catch (IOException e) {
            LOG.error("IOException converting file contents to byte array", e);
            throw new CreationException(e);
        }
    }


    @Override
    public boolean setSaveWith(StoreIdentifier identifier) {
        return storeId.setValue(identifier);
    }

    @Override
    public boolean setSaveWith(JavaCMUser user) {
        return setSaveWith(StoreIdentifier.StoreIdentifier(user));
    }

    @Override
    public StoreIdentifier getSavedWith() {
        return storeId.value(StoreIdentifier.DEFAULT);
    }

    public ObjectLevel getSaveLevel() {
        return getSavedWith().getObjectLevel();
    }

    @Override
    public boolean isOnLevel(ObjectLevel level) {
        return getSavedWith().isLevel(level);
    }

    @Override
    public boolean isUserLevel() {
        return isOnLevel(ObjectLevel.USER);
    }

    @Override
    public boolean isApplicationLevel() {
        return isOnLevel(ObjectLevel.APPLICATION);
    }

    @Override
    public void save() throws ConversionException, CreationException {
        save(CMCallback.<FileCreationResponse>doNothing());
    }

    @Override
    /**
     * Save this File
     * @param callback a {@link com.cloudmine.api.rest.callbacks.FileCreationResponseCallback}
     */
    public void save(Callback<FileCreationResponse> callback) throws CreationException, ConversionException {
        store().saveFile(this, callback);
    }

    @Override
    public void delete() {
        delete(CMCallback.<ObjectModificationResponse>doNothing());
    }

    @Override
    public void delete(Callback<ObjectModificationResponse> callback) {
        switch(getSaveLevel()) {
            case APPLICATION:
                store().deleteApplicationFile(getFileId(), callback);
                break;
            case USER:
                store().deleteUserFile(getFileId(), callback);
                break;
        }
    }

    @Override
    public JavaCMUser getUser() {
        return getSavedWith().getUser();
    }

    private CMStore store() throws CreationException {
        return CMStore.getStore(storeId.value(StoreIdentifier.DEFAULT));
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
    public String getFileId() {
        return fileId;
    }

    @Override
    public String getObjectId() {
        return getFileId();
    }


    /**
     * The MIME type for this file
     * @return The MIME type for this file; if it was not specified, "application/octet-stream" is assumed
     */
    public String getMimeType() {
        return contentType;
    }


    /**
     * The MIME type for this file
     * @return The MIME type for this file; if it was not specified, "application/octet-stream" is assumed
     * @deprecated in favor of the more accurately named getMimeType
     */
    @Deprecated
    public String getContentType() {
        return getMimeType();
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
        hash = hash * 31 + fileId.hashCode();
        hash = hash * 31 + Arrays.hashCode(fileContents);
        return hash;
    }

    @Override
    public final boolean equals(Object other) {
        if(other instanceof CMFile) {
            CMFile otherFile = (CMFile)other;
            return otherFile.getMimeType().equals(getMimeType()) &&
                    otherFile.getFileId().equals(getFileId()) &&
                    Arrays.equals(otherFile.getFileContents(), getFileContents());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("Key: ").append(fileId).append(" Content-Type: ").append(contentType).append(" contents: ");
        for(int i = 0; i < fileContents.length; i++) {
            string.append(fileContents[i]);
        }
        return string.toString();
    }

    @Override
    public String transportableRepresentation() throws ConversionException {
        return
             JsonUtilities.jsonCollection(
                JsonUtilities.createJsonProperty("key", fileId),
                JsonUtilities.createJsonProperty("content_type", contentType),
                JsonUtilities.createJsonProperty(JsonUtilities.TYPE_KEY, CMType.FILE.transportableRepresentation())).transportableRepresentation();

    }
}
