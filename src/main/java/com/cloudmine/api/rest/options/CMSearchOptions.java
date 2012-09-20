package com.cloudmine.api.rest.options;

import com.cloudmine.api.DistanceUnits;
import com.cloudmine.api.Immutable;
import com.cloudmine.api.rest.BaseURL;

/**
 * Options for searching, specifically returning distance information from
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSearchOptions implements BaseURL {
    public static final CMSearchOptions NONE = new CMSearchOptions("");
    private final DistanceUnits units;
    private final Immutable<String> urlString = new Immutable<String>();

    public CMSearchOptions(DistanceUnits units) {
        this.units = units;
    }

    public CMSearchOptions(String url) {
        this.units = null;
        urlString.setValue(url);
    }


    @Override
    public String asUrlString() {
        boolean isNotSet = !urlString.isSet();
        if(isNotSet) {
            urlString.setValue("distance=true&units=" + units.toString());
        }
        return urlString.value();
    }
}
