package com.cloudmine.api.rest.response;

import com.loopj.android.http.ResponseConstructor;
import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/4/12, 5:28 PM
 */
public class FileCreationResponse extends CloudMineResponse {

    public static final ResponseConstructor<FileCreationResponse> CONSTRUCTOR = new ResponseConstructor<FileCreationResponse>() {
        @Override
        public FileCreationResponse construct(HttpResponse response) {
            return new FileCreationResponse(response);
        }

        @Override
        public Future<FileCreationResponse> constructFuture(Future<HttpResponse> futureResponse) {
            return CloudMineResponse.createFutureResponse(futureResponse, this);
        }
    };

    public FileCreationResponse(HttpResponse response) {
        super(response);
    }

    public String getFileKey() {
        Object key = getObject("key");
        if(key == null) {
            return null;
        }
        return key.toString();
    }
}
