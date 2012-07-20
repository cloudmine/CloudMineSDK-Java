package com.cloudmine.api;

import com.cloudmine.api.rest.*;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class LibrarySpecificClassCreator {

    private static LibrarySpecificClassCreator creator;

    private Base64Encoder encoder;
    private HeaderFactory headerFactory;
    private AsynchronousHttpClient httpClient;

    public static LibrarySpecificClassCreator getCreator() {
        if(creator == null) {
            creator = new LibrarySpecificClassCreator(new Base64EncoderStandardImpl(),
                    new HeaderFactoryStandardImpl(),
                    new ApacheAsyncHttpClient());
        }
        return creator;
    }

    public static void setCreator(LibrarySpecificClassCreator creator) {
        LibrarySpecificClassCreator.creator = creator;
    }

    public LibrarySpecificClassCreator(Base64Encoder encoder, HeaderFactory headerFactory, AsynchronousHttpClient httpClient) {
        this.encoder = encoder;
        this.headerFactory = headerFactory;
        this.httpClient = httpClient;
    }

    public Base64Encoder getEncoder() {
        return encoder;
    }

    public HeaderFactory getHeaderFactory() {
        return headerFactory;
    }

    public AsynchronousHttpClient getAsynchronousHttpClient() {
        return httpClient;
    }
}
