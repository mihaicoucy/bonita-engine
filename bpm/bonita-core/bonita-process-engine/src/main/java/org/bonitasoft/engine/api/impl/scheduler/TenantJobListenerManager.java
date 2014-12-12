/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
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
 ******************************************************************************/

package org.bonitasoft.engine.api.impl.scheduler;

import java.util.List;

import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;

/**
 * Manage {@link org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener}s
 *
 * @author Elias Ricken de Medeiros
 */
public class TenantJobListenerManager {

    private final SchedulerService schedulerService;

    public TenantJobListenerManager(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    /**
     * Register {@link org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener}s for the given tenant
     *
     * @param listeners
     * @param tenantId
     * @throws SSchedulerException
     */
    public void registerListeners(List<AbstractBonitaTenantJobListener> listeners, long tenantId) throws SSchedulerException {
        if (!listeners.isEmpty()) {
            schedulerService.addJobListener(listeners, String.valueOf(tenantId));
        }
    }

}
