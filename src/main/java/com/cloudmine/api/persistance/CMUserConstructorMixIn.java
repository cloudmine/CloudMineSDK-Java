package com.cloudmine.api.persistance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMUserConstructorMixIn {
    @JsonCreator CMUserConstructorMixIn(@JsonProperty("email") String email,
                                        @JsonProperty("password") String password) {}
}
