package com.cloudmine.api;

import com.cloudmine.api.exceptions.AccessException;

/**
 * A field that can only be set to a non null value once
 *
 * @param <T> the type of the field
 */
public class Immutable<T>
{
    private T value = null;

    public Immutable(T value) {
        this.value = value;
    }

    public Immutable(){}

    /**
     * True if the value has been set, false otherwise
     * @return
     */
    public boolean isSet() {
        return value != null;
    }

    public T value() { return value; }

    public T valueOrThrow() throws AccessException {
        T value = value();
        if(value == null) {
            throw new AccessException("Value has not yet been set");
        }
        return value;
    }

    public T value(T alternative) {
        T value = value();
        return value == null ?
                alternative :
                value;
    }
    /**
     * Set the value. Returns true if the value was set, false otherwise
     * @param value
     * @return
     */
    public synchronized boolean setValue(T value) {
        if(this.value == null && value != null) {
            this.value = value;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        if(isSet())
            return "Immutable: " + value.toString();
        return "Immutable: unset";
    }

    @Override
    public int hashCode() {
        if(isSet())
            return value.hashCode();
        return 13;
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof Immutable) {
            Immutable otherI = (Immutable)other;
            if(otherI.isSet())
                if(isSet())
                    return value.equals(otherI.value());
        }
        return false;
    }
}
