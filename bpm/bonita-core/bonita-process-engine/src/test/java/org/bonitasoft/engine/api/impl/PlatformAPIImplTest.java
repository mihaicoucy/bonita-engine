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
package org.bonitasoft.engine.api.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.JobRegister;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformAPIImplTest {

    public static final long TENANT_ID = 56423L;
    @Mock
    private PlatformServiceAccessor platformServiceAccessor;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private SessionService sessionService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private NodeConfiguration platformConfiguration;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Mock
    private TenantConfiguration tenantConfiguration;
    @Mock
    private BonitaHomeServer bonitaHomeServer;

    private final List<STenant> tenants = Collections.singletonList(mock(STenant.class));

    private final List<AbstractBonitaTenantJobListener> tenantJobListeners = Collections.singletonList(mock(AbstractBonitaTenantJobListener.class));

    private final List<AbstractBonitaPlatformJobListener> platformJobListeners = Collections.singletonList(mock(AbstractBonitaPlatformJobListener.class));

    @Spy
    @InjectMocks
    private PlatformAPIImpl platformAPI;

    @Before
    public void setup() throws Exception {
        doReturn(schedulerService).when(platformServiceAccessor).getSchedulerService();
        doReturn(platformConfiguration).when(platformServiceAccessor).getPlatformConfiguration();
        doReturn(platformJobListeners).when(platformConfiguration).getJobListeners();

        doReturn(schedulerService).when(tenantServiceAccessor).getSchedulerService();
        doReturn(sessionService).when(tenantServiceAccessor).getSessionService();
        doReturn(tenantConfiguration).when(tenantServiceAccessor).getTenantConfiguration();
        doReturn(tenantJobListeners).when(tenantConfiguration).getJobListeners();

        doReturn(platformServiceAccessor).when(platformAPI).getPlatformAccessor();
        doReturn(sessionAccessor).when(platformAPI).createSessionAccessor();
        doReturn(tenantServiceAccessor).when(platformAPI).getTenantServiceAccessor(anyLong());
        doReturn(-1L).when(platformAPI).createSession(anyLong(), any(SessionService.class));
        doReturn(tenants).when(platformAPI).getTenants(platformServiceAccessor);
        doReturn(bonitaHomeServer).when(platformAPI).getBonitaHomeServerInstance();
        PlatformAPIImpl.isNodeStarted = false;
    }

    @Test
    public void rescheduleErroneousTriggers_should_call_rescheduleErroneousTriggers() throws Exception {
        platformAPI.rescheduleErroneousTriggers();

        verify(schedulerService).rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void rescheduleErroneousTriggers_should_throw_exception_when_rescheduleErroneousTriggers_failed() throws Exception {
        doThrow(new SSchedulerException("failed")).when(schedulerService).rescheduleErroneousTriggers();

        platformAPI.rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void rescheduleErroneousTriggers_should_throw_exception_when_cant_getPlatformAccessor() throws Exception {
        doThrow(new IOException()).when(platformAPI).getPlatformAccessor();

        platformAPI.rescheduleErroneousTriggers();
    }

    @Test
    public void startNode_should_call_startScheduler_when_node_is_not_started() throws Exception {
        // Given
        doNothing().when(platformAPI).checkPlatformVersion(platformServiceAccessor);
        doNothing().when(platformAPI).startPlatformServices(platformServiceAccessor);
        doReturn(false).when(platformAPI).isNodeStarted();
        doNothing().when(platformAPI).beforeServicesStartOfRestartHandlersOfTenant(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).startServicesOfTenants(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).restartHandlersOfPlatform(platformServiceAccessor);
        doNothing().when(platformAPI).afterServicesStartOfRestartHandlersOfTenant(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // When
        platformAPI.startNode();

        // Then
        verify(platformAPI).startScheduler(platformServiceAccessor, tenants);
    }

    @Test
    public void startNode_should_not_call_startScheduler_when_node_is_started() throws Exception {
        // Given
        doNothing().when(platformAPI).checkPlatformVersion(platformServiceAccessor);
        doNothing().when(platformAPI).startPlatformServices(platformServiceAccessor);
        doReturn(true).when(platformAPI).isNodeStarted();
        doNothing().when(platformAPI).startServicesOfTenants(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // When
        platformAPI.startNode();

        // Then
        verify(platformAPI, never()).startScheduler(platformServiceAccessor, tenants);
    }

    @Test
    public void startNode_should_call_registerMissingTenantsDefaultJobs() throws Exception {
        // Given
        doNothing().when(platformAPI).checkPlatformVersion(platformServiceAccessor);
        doNothing().when(platformAPI).startPlatformServices(platformServiceAccessor);
        doReturn(true).when(platformAPI).isNodeStarted();
        doNothing().when(platformAPI).startServicesOfTenants(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // When
        platformAPI.startNode();

        // Then
        verify(platformAPI).registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);
    }

    @Test
    public void registerMissingTenantsDefaultJobs_should_call_registerJob_when_job_is_missing() throws BonitaHomeNotSetException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
            SBonitaException, IOException, ClassNotFoundException {
        // Given
        final TransactionService transactionService = mock(TransactionService.class);
        doReturn(transactionService).when(platformServiceAccessor).getTransactionService();
        doNothing().when(transactionService).begin();
        doNothing().when(transactionService).complete();
        final JobRegister jobRegister = mock(JobRegister.class);
        doReturn("newJob").when(jobRegister).getJobName();
        final List<JobRegister> defaultJobs = Collections.singletonList(jobRegister);
        doReturn(defaultJobs).when(tenantConfiguration).getJobsToRegister();
        final List<String> scheduledJobNames = Collections.singletonList("someOtherJob");
        doReturn(scheduledJobNames).when(schedulerService).getJobs();
        doNothing().when(platformAPI).registerJob(schedulerService, jobRegister);

        // When
        platformAPI.registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // Then
        verify(platformAPI).registerJob(schedulerService, jobRegister);
    }

    @Test
    public void registerMissingTenantsDefaultJobs_should_not_call_registerJob_when_job_is_scheduled() throws BonitaHomeNotSetException,
            BonitaHomeConfigurationException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
            SBonitaException, IOException, ClassNotFoundException {
        // Given
        final TransactionService transactionService = mock(TransactionService.class);
        doReturn(transactionService).when(platformServiceAccessor).getTransactionService();
        doNothing().when(transactionService).begin();
        doNothing().when(transactionService).complete();
        final JobRegister jobRegister = mock(JobRegister.class);
        doReturn("existingJob").when(jobRegister).getJobName();
        final List<JobRegister> defaultJobs = Collections.singletonList(jobRegister);
        doReturn(defaultJobs).when(tenantConfiguration).getJobsToRegister();
        final List<String> scheduledJobNames = Collections.singletonList("existingJob");
        doReturn(scheduledJobNames).when(schedulerService).getJobs();
        doNothing().when(platformAPI).registerJob(schedulerService, jobRegister);

        // When
        platformAPI.registerMissingTenantsDefaultJobs(platformServiceAccessor, sessionAccessor, tenants);

        // Then
        verify(platformAPI, never()).registerJob(schedulerService, jobRegister);
    }

    @Test
    public void startScheduler_should_register_PlatformJobListeners_and_TenantJobListeners_when_scheduler_starts() throws Exception {
        // Given
        doReturn(true).when(platformConfiguration).shouldStartScheduler();
        doReturn(false).when(schedulerService).isStarted();

        // When
        platformAPI.startScheduler(platformServiceAccessor, tenants);

        // Then
        verify(schedulerService).initializeScheduler();
        verify(schedulerService).addJobListener(anyListOf(AbstractBonitaPlatformJobListener.class));
        verify(schedulerService).addJobListener(anyListOf(AbstractBonitaTenantJobListener.class), anyString());
        verify(schedulerService).start();
    }

    @Test
    public void startScheduler_should_not_register_PlatformJobListeners_and_TenantJobListeners_when_scheduler_is_started() throws Exception {
        // Given
        doReturn(true).when(platformConfiguration).shouldStartScheduler();
        doReturn(true).when(schedulerService).isStarted();

        // When
        platformAPI.startScheduler(platformServiceAccessor, tenants);

        // Then
        verify(schedulerService, never()).initializeScheduler();
        verify(schedulerService, never()).addJobListener(anyListOf(AbstractBonitaPlatformJobListener.class));
        verify(schedulerService, never()).addJobListener(anyListOf(AbstractBonitaTenantJobListener.class), anyString());
        verify(schedulerService, never()).start();
    }

    @Test
    public void startScheduler_should_not_register_PlatformJobListeners_and_TenantJobListeners_when_scheduler_should_not_be_started() throws Exception {
        // Given
        doReturn(false).when(platformConfiguration).shouldStartScheduler();
        doReturn(false).when(schedulerService).isStarted();

        // When
        platformAPI.startScheduler(platformServiceAccessor, tenants);

        // Then
        verify(schedulerService, never()).initializeScheduler();
        verify(schedulerService, never()).addJobListener(anyListOf(AbstractBonitaPlatformJobListener.class));
        verify(schedulerService, never()).addJobListener(anyListOf(AbstractBonitaTenantJobListener.class), anyString());
        verify(schedulerService, never()).start();
    }

    @Test
    public void startScheduler_should_not_register_JobListeners_when_none_are_configured() throws Exception {
        // Given
        doReturn(true).when(platformConfiguration).shouldStartScheduler();
        doReturn(false).when(schedulerService).isStarted();
        doReturn(Collections.EMPTY_LIST).when(platformConfiguration).getJobListeners();
        doReturn(Collections.EMPTY_LIST).when(tenantConfiguration).getJobListeners();

        // When
        platformAPI.startScheduler(platformServiceAccessor, tenants);

        // Then
        verify(schedulerService).initializeScheduler();
        verify(schedulerService, never()).addJobListener(anyListOf(AbstractBonitaPlatformJobListener.class));
        verify(schedulerService, never()).addJobListener(anyListOf(AbstractBonitaTenantJobListener.class), anyString());
        verify(schedulerService).start();
    }

    @Test
    public void should_updateTenantPortalConfigurationFile_call_bonitaHomeServer() throws Exception {
        //when
        platformAPI.updateClientTenantConfigurationFile(TENANT_ID, "myProps.properties", "updated content".getBytes());
        //then
        verify(bonitaHomeServer).updateTenantPortalConfigurationFile(TENANT_ID, "myProps.properties", "updated content".getBytes());
    }
}
