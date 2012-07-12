package com.cloudmine.api.persistance;

import com.cloudmine.api.rest.JsonUtilities;
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
    @Override
    public void discovered(String className, String annotationName) {
        try {
            Class klass = Class.forName(className);

            JsonUtilities.addCMUserMixinsTo(klass);

            CloudMineObject annotation = (CloudMineObject)klass.getAnnotation(CloudMineObject.class);
            boolean useDefault = CloudMineObject.DEFAULT_VALUE.equals(annotation.className());
            String nameToUse = useDefault ?
                    className :
                    annotation.className();
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
