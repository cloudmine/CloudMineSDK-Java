package com.cloudmine.test;

import com.cloudmine.api.CMUser;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.persistance.CloudMineObject;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
@CloudMineObject
public class ExtendedCMUser extends CMUser {

    private String address;
    private int age;
    private boolean isPaid;


    public ExtendedCMUser(String email, String password) throws CreationException {
        super(email, password);
        address = "123 Real St, Philadelphia, PA 19123";
        age = 42;
        isPaid = false;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }
}
