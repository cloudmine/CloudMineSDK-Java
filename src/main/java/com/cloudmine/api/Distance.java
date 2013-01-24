package com.cloudmine.api;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class Distance {

    public static final DistanceUnits DEFAULT_UNITS = DistanceUnits.km;

    private final double measurement;
    private final DistanceUnits units;

    public Distance(double measurement, DistanceUnits units) {
        if(units == null)
            throw new NullPointerException("Can't have a distance with null units");
        this.measurement = measurement;
        this.units = units;
    }

    public Distance(double measurement) {
        this(measurement, DEFAULT_UNITS);
    }

    public double getMeasurement() {
        return measurement;
    }

    public DistanceUnits getUnits() {
        return units;
    }

    public String toString() {
        return measurement + units.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Distance distance = (Distance) o;

        if (Double.compare(distance.measurement, measurement) != 0) return false;
        if (units != distance.units) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = measurement != +0.0d ? Double.doubleToLongBits(measurement) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + units.hashCode();
        return result;
    }
}
