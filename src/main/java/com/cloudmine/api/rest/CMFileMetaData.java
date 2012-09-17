package com.cloudmine.api.rest;

import com.cloudmine.api.CMObject;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMFileMetaData extends CMObject {
    @JsonProperty("content_type")
    private String contentType;
    private String filename;

    public CMFileMetaData() {
        super();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
