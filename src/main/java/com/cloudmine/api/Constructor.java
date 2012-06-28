package com.cloudmine.api;

import java.util.concurrent.Future;

/**
 * Encapsulates creating one object from another
 * Copyright CloudMine LLC
 */
public interface Constructor<F, T> {

    public T construct(F from);

    public Future<T> constructFuture(Future<F> from);
}
