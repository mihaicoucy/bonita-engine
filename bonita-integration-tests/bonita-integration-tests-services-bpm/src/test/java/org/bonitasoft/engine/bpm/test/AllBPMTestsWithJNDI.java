/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bpm.test;

import static org.bonitasoft.engine.bpm.CommonBPMServicesTest.getServicesBuilder;

import javax.naming.Context;

import org.bonitasoft.engine.api.impl.PlatformAPIImpl;
import org.bonitasoft.engine.core.form.impl.FormMappingServiceIT;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.util.TestUtil;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(Suite.class)
@SuiteClasses({
        AllBPMTests.class
})
public class AllBPMTestsWithJNDI {

    static ConfigurableApplicationContext springContext;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.err.println("=================== AllBPMTests.beforeClass()");
        setupSpringContext();

        final PlatformAPIImpl platformAPI = new PlatformAPIImpl();
        getServicesBuilder().getSessionAccessor().setSessionInfo(1l, -1);
        platformAPI.createAndInitializePlatform();
        platformAPI.startNode();

    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.err.println("=================== AllBPMTests.afterClass()");

        TransactionService transactionService = getServicesBuilder().getTransactionService();
        TestUtil.closeTransactionIfOpen(transactionService);
        // stopScheduler();
        PlatformService platformService = getServicesBuilder().getPlatformService();
        SessionAccessor sessionAccessor = getServicesBuilder().getSessionAccessor();
        TestUtil.deleteDefaultTenantAndPlatForm(transactionService, platformService, sessionAccessor, getServicesBuilder().getSessionService());

        closeSpringContext();
    }

    private static void setupSpringContext() {
        setSystemPropertyIfNotSet("sysprop.bonita.db.vendor", "h2");

        // Force these system properties
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.bonitasoft.engine.local.SimpleMemoryContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.bonitasoft.engine.local");

        springContext = new ClassPathXmlApplicationContext("datasource.xml", "jndi-setup.xml");
    }

    private static void closeSpringContext() {
        springContext.close();
    }

    private static void setSystemPropertyIfNotSet(final String property, final String value) {
        System.setProperty(property, System.getProperty(property, value));
    }
}
