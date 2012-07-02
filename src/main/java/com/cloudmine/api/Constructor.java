package com.cloudmine.api;

/**
 * Encapsulates creating one object from another
 * Copyright CloudMine LLC
 */
public interface Constructor<F, T> {

    public T construct(F from);
}
