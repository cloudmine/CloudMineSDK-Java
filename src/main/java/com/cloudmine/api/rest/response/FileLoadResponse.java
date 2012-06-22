package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMFile;
import com.cloudmine.api.exceptions.CreationException;
import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * User: johnmccarthy
 * Date: 6/20/12, 3:45 PM
 */
public class FileLoadResponse extends CMResponse {


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
        public Future<FileLoadResponse> constructFuture(Future<HttpResponse> futureResponse) {
            return CMResponse.createFutureResponse(futureResponse, this);
        }
    };

    public static ResponseConstructor<FileLoadResponse> constructor(String fileName) {
        return new Constructor(fileName);
    }
    private final CMFile file;

    public FileLoadResponse(HttpResponse response, String fileName) {
        super(response);
        file = CMFile.CMFile(response, fileName);
    }

    public CMFile getFile() {
        return file;
    }
}
