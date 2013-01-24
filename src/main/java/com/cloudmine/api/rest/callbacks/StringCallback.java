package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.response.ResponseConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class StringCallback extends CMCallback<String> {

    public static final ResponseConstructor<String> CONSTRUCTOR = new ResponseConstructor<String>() {
        @Override
        public String construct(HttpResponse response) throws CreationException {

            InputStream jsonStream = null;
            StringWriter writer = new StringWriter();
            try {
                jsonStream = response.getEntity().getContent();
                IOUtils.copy(jsonStream, writer, JsonUtilities.ENCODING);
            } catch (IOException e) {
                throw new CreationException("Error reading in message body", e);
            }

            String messageBody = writer.toString();
            return messageBody;
        }
    };
    public StringCallback() {
        super(CONSTRUCTOR);
    }
}
