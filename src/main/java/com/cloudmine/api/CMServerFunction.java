package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.BaseURL;
import com.cloudmine.api.rest.JsonUtilities;

import java.util.Collections;
import java.util.Map;

/**
 * Encapsulates options and snippet name information for making server side function calls. For more information,
 * see <a href="https://cloudmine.me/docs/custom-code">the CloudMine documentation on executing custom code</a>
 * Copyright CloudMine LLC
 */
public class CMServerFunction implements BaseURL{
    public static final CMServerFunction NONE = new CMServerFunction("");
    public static final boolean DEFAULT_ASYNC = false;
    public static final boolean DEFAULT_RESULTS_ONLY = false;
    public static final Map<String,String> DEFAULT_EXTRA_PARAMETERS = Collections.<String, String>emptyMap();

    private final String snippetName;
    private final boolean resultsOnly;
    private final boolean isAsynchronous;
    private final Map<String, String> extraParameters;
    private final Immutable<String> urlString = new Immutable<String>();

    /**
     * Instantiate a new CMServerFunction as a raw string that will be appended to the URL. No leading & or ? necessary.
     * You probably don't want to be calling this
     * @param asString the raw URL string that will be appended to the request URL
     */
    public CMServerFunction(String asString) {
        urlString.setValue(asString);
        snippetName = "";
        resultsOnly = false;
        isAsynchronous = false;
        extraParameters = Collections.emptyMap();
    }

    /**
     * Instantiate a new CMServerFunction that calls into the given snippetName on completion of the request.
     * Has no extra parameters and is not asynchronous.
     * @param snippetName the name of the snippet, defined in your CloudMine dashboard
     * @param resultsOnly if true, only the results of the function call are returned, otherwise the original data is returned as well
     */
    public CMServerFunction(String snippetName, boolean resultsOnly) {
        this(snippetName, resultsOnly, DEFAULT_ASYNC, DEFAULT_EXTRA_PARAMETERS);
    }

    /**
     * Instantiate a new CMServerFunction that calls into the given snippetName on completion of the request.
     * Has no extra parameters and is not asynchronous.
     * @param snippetName the name of the snippet, defined in your CloudMine dashboard
     * @param resultsOnly if true, only the results of the function call are returned, otherwise the original data is returned as well
     * @param isAsynchronous if true, run the code snippet asynchronously; this will cause the HTTP call to return immediately but will leave your code running on our servers. The only way to return data from an asynchronous call is to save it back using the CloudMine API from within the function. When false (default), the HTTP call will not complete until the function is done running.
     * @param extraParameters Allows you to pass in arbitrary parameters to the function. They will be available as data.params. If specified as valid JSON, they will already be parsed as a JSON object.
     */
    public CMServerFunction(String snippetName, boolean resultsOnly, boolean isAsynchronous, Map<String, String> extraParameters) {
        if(snippetName == null) {
            throw new CreationException("Cannot call a null function!");
        }
        this.snippetName = snippetName;
        this.resultsOnly = resultsOnly;
        this.isAsynchronous = isAsynchronous;
        this.extraParameters = Collections.unmodifiableMap(extraParameters);
    }

    @Override
    public String urlString() {
        boolean isNotSet = urlString.isSet() == false;
        if(isNotSet) {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append("f=").append(snippetName).append("&result_only=").append(resultsOnly).append("&async=").append(isAsynchronous);
            boolean haveExtraParameters = !extraParameters.isEmpty();
            if(haveExtraParameters) {
                urlBuilder.append("&params=");
                urlBuilder.append(JsonUtilities.mapToJson(extraParameters));
            }
            urlString.setValue(urlBuilder.toString());
        }
        return urlString.value();
    }
}
