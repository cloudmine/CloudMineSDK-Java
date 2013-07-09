package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMFile;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.response.code.FileLoadCode;
import org.apache.http.HttpResponse;

/**
 * Return in response to a file load request. If the request was successful, contains the {@link CMFile} requested
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class FileLoadResponse extends ResponseBase<FileLoadCode> {
    public static class Constructor implements ResponseConstructor<FileLoadResponse> {
        private final String key;
        public Constructor(String key) {
            super();
            this.key = key;
        }

        @Override
        public FileLoadResponse construct(HttpResponse response) throws CreationException {
            return new FileLoadResponse(response, key);
        }

        @Override
        public FileLoadResponse construct(String messageBody, int responseCode) throws CreationException {
            return null;  //TODO can't handle this like the rest
        }
    };

    public static ResponseConstructor<FileLoadResponse> constructor(String fileId) {
        return new Constructor(fileId);
    }

    private final CMFile file;

    public FileLoadResponse(HttpResponse response, String fileId) {
        super(response, false);
        file = new CMFile(response, fileId);
    }

    @Override
    public FileLoadCode getResponseCode() {
        return FileLoadCode.codeForStatus(getStatusCode());
    }

    public CMFile getFile() {
        return file;
    }
}
