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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;


        ExtendedCMUser that = (ExtendedCMUser) o;

        if (age != that.age) return false;
        if (isPaid != that.isPaid) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + age;
        result = 31 * result + (isPaid ? 1 : 0);
        return result;
    }
}
