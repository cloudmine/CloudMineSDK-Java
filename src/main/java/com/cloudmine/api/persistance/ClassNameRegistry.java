package com.cloudmine.api.persistance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ClassNameRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(ClassNameRegistry.class);
    private static final ClassNameRegistry registry = new ClassNameRegistry();

    private final Map<String, Class> registryMap = new HashMap<String, Class>();
    //why is there no BidiMap as a default in java? not writing me own, but if this gets more complicated it should be done
    private final Map<Class, String> reverseRegistryMap = new HashMap<Class, String>();

    public static void register(String name, Class klass) {
        registry.registryMap.put(name, klass);
        registry.reverseRegistryMap.put(klass, name);
    }

    public static Class forName(String name) {
        Class aClass = registry.registryMap.get(name);
        if(aClass == null) {
            try {
                aClass = Class.forName(name);
            } catch (ClassNotFoundException e) {
                LOG.error("Exception thrown", e);
            }
        }
        return aClass;
    }

    public static String forClass(Class klass) {
        String className = registry.reverseRegistryMap.get(klass);
        if(className == null) {
            className = klass.getName();
        }
        return className;
    }

    public static boolean isRegistered(String klass) {
        boolean isRegistered = registry.registryMap.containsKey(klass);
        if(isRegistered)
            return true;
        try {
            Class.forName(klass);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
