package com.cloudmine.api;

import com.cloudmine.test.ExtendedCMObject;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMObjectTest {

    @Test
    public void testNoAccessOnApplicationLevelObjects() {
        ExtendedCMObject object = new ExtendedCMObject();

        assertFalse(object.transportableRepresentation().contains(CMObject.ACCESS_KEY));
    }
}
