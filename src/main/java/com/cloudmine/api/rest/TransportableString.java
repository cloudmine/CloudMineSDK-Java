package com.cloudmine.api.rest;

/**
 * An immutable string that is valid transportable representation
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class TransportableString implements Transportable {

    private final String json;

    /**
     * Instantiate a new TransportableString whose transportableRepresentation method will return the passed in String.
     * @param transport a valid transport string. If null, will be replaced with {@link JsonUtilities#EMPTY_JSON}
     */
    public TransportableString(String transport) {
        if(transport == null)
            transport = JsonUtilities.EMPTY_JSON;
        this.json = transport;
    }

    @Override
    public String transportableRepresentation() {
        return json;
    }

    @Override
    public String toString() {
        return transportableRepresentation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransportableString that = (TransportableString) o;

        if (json != null ? !json.equals(that.json) : that.json != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return json != null ? json.hashCode() : 0;
    }
}
