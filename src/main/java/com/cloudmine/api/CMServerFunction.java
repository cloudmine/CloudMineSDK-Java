package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.BaseURL;
import com.cloudmine.api.rest.JsonUtilities;

import java.util.Collections;
import java.util.Map;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/17/12, 3:20 PM
 */
public class CMServerFunction implements BaseURL{
    public static final CMServerFunction NONE = new CMServerFunction("");
    
    private final String snippetName;
    private final boolean resultsOnly;
    private final boolean isAsynchronous;
    private final Map<String, String> extraParameters;
    private final Immutable<String> urlString = new Immutable<String>();

    public CMServerFunction(String asString) {
        urlString.setValue(asString);
        snippetName = "";
        resultsOnly = false;
        isAsynchronous = false;
        extraParameters = Collections.emptyMap();
    }

    public CMServerFunction(String snippetName, boolean resultsOnly) {
        this(snippetName, resultsOnly, false, Collections.<String, String>emptyMap());
    }
    
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
