package com.cloudmine.api;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class Strings {

    public static final String QUOTE = "\"";

    public static boolean isEmpty(String string) {
        return string == null || string.length() == 0;
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }

    public static String asString(Object obj) {
        return obj == null ?
                "" :
                obj.toString();
    }
}
