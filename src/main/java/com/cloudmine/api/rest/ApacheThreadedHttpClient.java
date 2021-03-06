package com.cloudmine.api.rest;

import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.ResponseConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ApacheThreadedHttpClient implements AsynchronousHttpClient {
    private static final Logger LOG = LoggerFactory.getLogger(ApacheThreadedHttpClient.class);
    private static final int RETRY_REQUEST_COUNT = 4;


    private HttpContext httpContext = new SyncBasicHttpContext(new BasicHttpContext());
    private ClientConnectionManager connectionManager;
    {
        try {
            //this will work for 4.2 and later
            Class poolingClientConnectionManagerClass = Class.forName("org.apache.http.impl.conn.PoolingClientConnectionManager");
            connectionManager = (ClientConnectionManager) poolingClientConnectionManagerClass.getConstructor().newInstance();
        } catch (Exception e) {
            //we are running on an older version, lets try the  backup
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(
                    new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(
                    new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            connectionManager = new ThreadSafeClientConnManager(new BasicHttpParams(), schemeRegistry);
        }
    }

    private DefaultHttpClient client = new DefaultHttpClient(connectionManager, null);

    @Override
    public <T> void executeCommand(HttpUriRequest command, Callback<T> callback, ResponseConstructor<T> constructor) {
        new Thread(new RequestRunnable<T>(command, callback, constructor)).start();
    }

    public class RequestRunnable<T>implements Runnable {

        private final Callback<T> callback;
        private final ResponseConstructor<T> constructor;
        private RequestRunnable(HttpUriRequest request, Callback<T> callback, ResponseConstructor<T> constructor) {
            this.request = request;
            this.callback = callback;
            this.constructor = constructor;
        }
        private HttpUriRequest request;

        @Override
        public void run() {
            boolean retry = true;
            IOException cause = null;
            int requestCounter = 0;
            HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();

            while(retry &&
                    requestCounter < RETRY_REQUEST_COUNT) {
                try {
                    makeRequest();
                    return;
                } catch (IOException e) {
                    LOG.error("Exception thrown", e);
                    cause = e;
                    requestCounter++;
                    retry = retryHandler.retryRequest(cause, requestCounter, httpContext);
                } catch(NullPointerException e) {
                    // there's a bug in HttpClient 4.0.x that on some occasions causes
                    // DefaultRequestExecutor to throw an NPE, see
                    // http://code.google.com/p/android/issues/detail?id=5255
                    cause = new IOException("NPE");
                    requestCounter++;
                    retry = retryHandler.retryRequest(cause, requestCounter, httpContext);
                } catch(Exception e) {
                    callback.onFailure(e, "Failed");
                    return;
                }
            }
            callback.onFailure(cause, "Failed");
        }

        private void makeRequest() throws IOException {
            HttpResponse response = client.execute(request);
            try {
                ResponseTimeDataStore.extractAndStoreResponseTimeInformation(callback, response);
            }finally {
                callback.onCompletion(constructor.construct(response));
            }
        }
    }
}
