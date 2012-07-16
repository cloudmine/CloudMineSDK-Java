package com.cloudmine.test;

import com.cloudmine.api.CMObject;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class SimpleExtendedCMObject extends CMObject {

    private int number;
    private String string;

    public SimpleExtendedCMObject() {
        this(5, "dog");
    }

    public SimpleExtendedCMObject(int number, String string) {
        this.number = number;
        this.string = string;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
