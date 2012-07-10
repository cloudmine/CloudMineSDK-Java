package com.cloudmine.api.persistance;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CloudMineObject {
    public static final String DEFAULT_VALUE = "Unset";

    String value() default DEFAULT_VALUE;
}
