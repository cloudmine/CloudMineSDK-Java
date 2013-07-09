package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.response.code.FileCreationCode;
import org.apache.http.HttpResponse;

/**
 * Returned by the CloudMine service in response to file creation requests. Contains the fileId
 * for the created file
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class FileCreationResponse extends ResponseBase<FileCreationCode> {

    public static final ResponseConstructor<FileCreationResponse> CONSTRUCTOR = new ResponseConstructor<FileCreationResponse>() {
        @Override
        public FileCreationResponse construct(HttpResponse response) {
            return new FileCreationResponse(response);
        }

        @Override
        public FileCreationResponse construct(String messageBody, int responseCode) throws CreationException {
            return new FileCreationResponse(messageBody, responseCode);
        }
    };

    /**
     * Instantiate a new FileCreationResponse. You should probably not be calling this yourself
     * @param response a response to a file creation request
     */
    public FileCreationResponse(HttpResponse response) {
        super(response);
    }

    public FileCreationResponse(String message, int responseCode) {
        super(message, responseCode);
    }

    @Override
    public FileCreationCode getResponseCode() {
        return FileCreationCode.codeForStatus(getStatusCode());
    }

    /**
     * Get the name for the file. May be null if the create request failed
     * @return The name for the file. May be null if the create request failed
     */
    public String getfileId() {
        Object key = getObject("key");
        if(key == null) {
            return null;
        }
        return key.toString();
    }
}
