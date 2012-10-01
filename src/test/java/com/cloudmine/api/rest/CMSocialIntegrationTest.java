package com.cloudmine.api.rest;

import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.rest.callbacks.CMSocialResponseCallback;
import com.cloudmine.api.rest.response.CMSocialResponse;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Test;

import java.util.List;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSocialIntegrationTest extends ServiceTestBase {
    //There is no automated way to log in at the moment
    public static final String facebookKey = "RrEEUdq-rNctu03pXNe1cMOTVkY=anp7tCo-eed9ab9485919be60a07482bdded5bd4e7ccaaf840bae258290df4251c4c31b625c566a88567762cca6ad54cd1c95891ff909bef66ad79d37326e7b4b98ed4188fc9ecc2a2564ca61cc02eaf57a1527ca845e81cf4114fda7b9ba7d2bbdac42f754ed01e61cbad9b6a28720f14ecccb6";
    @Test
    public void getFacebookSelf() {
        CMSocial social = new CMSocial(CMSocial.Service.FACEBOOK, facebookKey);
        social.get(CMSocial.Service.FACEBOOK, CMSocial.Action.SELF, testCallback(new CMSocialResponseCallback() {
            public void onCompletion(CMSocialResponse response) {
                assertTrue(response.wasSuccess());
                List<SimpleCMObject> asSimpleObjects = response.getAsSimpleCMObjects();
                assertEquals(1, asSimpleObjects.size());

            }
        }));
        waitThenAssertTestResults();
    }
}
