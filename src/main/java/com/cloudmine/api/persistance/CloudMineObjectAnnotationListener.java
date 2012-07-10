package com.cloudmine.api.persistance;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationDiscoveryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers objects marked with {@link CloudMineObject}
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CloudMineObjectAnnotationListener implements ClassAnnotationDiscoveryListener {
    private static final Logger LOG = LoggerFactory.getLogger(CloudMineObjectAnnotationListener.class);

    public static void runAnnotationDiscoverer() {
        Discoverer discoverer = new ClasspathDiscoverer();
        discoverer.addAnnotationListener(new CloudMineObjectAnnotationListener());
        discoverer.discover();
    }

    @Override
    public void discovered(String className, String annotationName) {
        try {
            Class klass = Class.forName(className);

            CloudMineObject annotation = (CloudMineObject)klass.getAnnotation(CloudMineObject.class);
            boolean useDefault = CloudMineObject.DEFAULT_VALUE.equals(annotation.value());
            String nameToUse = useDefault ?
                    className :
                    annotation.value();
            ClassNameRegistry.register(nameToUse, klass);
        } catch (ClassNotFoundException e) {
            LOG.error("Could not find class that clearly exists: " + className + " for annotation: " + annotationName, e);
        }
    }

    @Override
    public String[] supportedAnnotations() {
        return new String[]{CloudMineObject.class.getName()};
    }
}
