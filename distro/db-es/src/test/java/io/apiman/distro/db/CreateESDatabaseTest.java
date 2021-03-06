/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.distro.db;

import io.apiman.manager.test.util.AbstractTestPlanTest;
import io.apiman.manager.test.util.ManagerTestUtils;
import io.apiman.manager.test.util.ManagerTestUtils.TestType;
import io.apiman.test.common.util.TestUtil;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test that creates an H2 database file by firing up the API Manager and sending a 
 * bunch of REST requests to configure it.  When this test is complete there should be a
 * valid H2 database located in target/classes (and thus be included in the JAR).
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class CreateESDatabaseTest extends AbstractTestPlanTest {

    @BeforeClass
    public static void setup() throws Exception {
        File targetClassesDir = new File("target/classes").getAbsoluteFile();
        if (!targetClassesDir.exists()) {
            targetClassesDir.mkdirs();
        }

        System.out.println("------------------------------------------------");
        System.out.println("Setting elasticsearch output path: " + targetClassesDir.toString());
        System.out.println("------------------------------------------------");

        TestUtil.setProperty("apiman.test.es-home", targetClassesDir.toString());
        TestUtil.setProperty("apiman.test.admin-user-only", "true");
        TestUtil.setProperty("apiman.test.es-cluster-name", "apiman");
        TestUtil.setProperty("apiman.test.es-persistence", "true");
        ManagerTestUtils.setTestType(TestType.es);
        AbstractTestPlanTest.setup();
    }
    
    @Test
    public void test() {
        try {
            TestUtil.setProperty("apiman.suite.api-username", "admin");
            TestUtil.setProperty("apiman.suite.api-password", "admin");
            TestUtil.setProperty("apiman.suite.gateway-config-endpoint", "http://localhost:8080/apiman-gateway-api");
            TestUtil.setProperty("apiman.suite.gateway-config-username", "apimanager");
            TestUtil.setProperty("apiman.suite.gateway-config-password", "apiman123!");
            
            runTestPlan("scripts/api-manager-init-testPlan.xml", CreateH2DatabaseTest.class.getClassLoader());
        } finally {
            System.clearProperty("apiman.test.h2-output-dir");
        }
    }

    @AfterClass
    public static void shutdown() throws Exception {
        Thread.sleep(2000); // allow ES time to refresh/index
        TestUtil.setProperty("apiman.test.es-delete-index", "false");
        AbstractTestPlanTest.shutdown();
        testServer.getESNode().close();
    }

}
