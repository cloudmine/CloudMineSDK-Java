package com.cloudmine.api.rest;



/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ApacheAsyncHttpClient {//implements AsynchronousHttpClient {
//    private static final Logger LOG = LoggerFactory.getLogger(ApacheAsyncHttpClient.class);
//    private final HttpAsyncClient client;
//
//    public ApacheAsyncHttpClient() {
//        HttpAsyncClient temp = null;
//        try {
//            temp = new DefaultHttpAsyncClient();
//        } catch (IOReactorException e) {
//            LOG.error("Exception thrown", e);
//        }
//
//        client = temp;
//        client.start();
//    }
//
//    @Override
//    public <T> void executeCommand(HttpUriRequest command, final Callback<T> callback, final ResponseConstructor<T> constructor) {
//        LOG.debug("Executing: " + command.getClass() + " at: " + command.getURI().toASCIIString());
//        client.execute(command, new FutureCallback<HttpResponse>() {
//                         @Override
//                         public void completed(HttpResponse result) {
//                                 callback.onCompletion(constructor.construct(result));
//                             }
//
//                                 @Override
//                         public void failed(Exception ex) {
//                                 callback.onFailure(ex, "failed");
//                             }
//
//                                 @Override
//                         public void cancelled() {
//                                 callback.onFailure(new Exception("Request was cancelled!"), "cancelled");
//                             }
//                     });
//    }
}
