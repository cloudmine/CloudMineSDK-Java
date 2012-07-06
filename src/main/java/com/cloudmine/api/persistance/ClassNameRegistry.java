package com.cloudmine.api.persistance;

import java.util.HashMap;
import java.util.Map;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ClassNameRegistry {

    private static final ClassNameRegistry registry = new ClassNameRegistry();

    private final Map<String, Class> registryMap = new HashMap<String, Class>();

    public static void register(String name, Class klass) {
        registry.registryMap.put(name, klass);
    }

    public static Class forName(String name) {
        return registry.registryMap.get(name);
    }

    public static boolean isRegistered(String klass) {
        return registry.registryMap.containsKey(klass);
    }
}
