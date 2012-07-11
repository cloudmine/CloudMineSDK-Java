package com.cloudmine.api.persistance;

import com.fasterxml.jackson.annotation.JacksonAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that can be used to define constructors and factory
 * methods as one to use for instantiating new instances of the associated
 * class.
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.<br>
 * Copy and pasta'd from Jackson
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface Reconstructor {
}
