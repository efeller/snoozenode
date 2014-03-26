/**
 * Copyright (C) 2010-2013 Eugen Feller, INRIA <eugen.feller@inria.fr>
 *
 * This file is part of Snooze, a scalable, autonomic, and
 * energy-aware virtual machine (VM) management framework.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package org.inria.myriads.snoozenode.localcontroller.monitoring;

import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerDescription;
import org.inria.myriads.snoozecommon.communication.virtualcluster.monitoring.NetworkDemand;
import org.inria.myriads.snoozenode.configurator.monitoring.HostMonitorSettings;
import org.inria.myriads.snoozenode.exception.HostMonitoringException;
import org.inria.myriads.snoozenode.exception.VirtualMachineMonitoringException;
import org.inria.myriads.snoozenode.localcontroller.connector.Connector;
import org.inria.myriads.snoozenode.localcontroller.monitoring.api.HostMonitor;
import org.inria.myriads.snoozenode.localcontroller.monitoring.api.VirtualMachineMonitor;
import org.inria.myriads.snoozenode.localcontroller.monitoring.api.impl.GangliaHostMonitor;
import org.inria.myriads.snoozenode.localcontroller.monitoring.api.impl.LibVirtHostMonitor;
import org.inria.myriads.snoozenode.localcontroller.monitoring.api.impl.LibVirtVirtualMachineMonitor;
import org.inria.myriads.snoozenode.util.PluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure monitoring factory.
 * 
 * @author Eugen Feller
 */
public final class MonitoringFactory 
{
    
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(MonitoringFactory.class);
    
    /**
     * Hide the consturctor.
     */
    private MonitoringFactory() 
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Creates the virtual machine monitoring.
     * 
     * @param connector                             The connector object
     * @return                                      The virtual machine monitor
     * @throws VirtualMachineMonitoringException 
     */
    public static VirtualMachineMonitor newVirtualMachineMonitor(Connector connector) 
        throws VirtualMachineMonitoringException
    {
        return new LibVirtVirtualMachineMonitor(connector);
    }
    
    /**
     * Creates the host monitoring.
     * 
     * @param connector                        The connector object
     * @param networkCapacity                  The network capacity
     * @return                                 The host monitor
     * @throws HostMonitoringException         The monitoring exception
     */
    public static LibVirtHostMonitor newHostMonitoring(Connector connector, NetworkDemand networkCapacity) 
        throws HostMonitoringException
    {
        return new LibVirtHostMonitor(connector, networkCapacity);
    }
    
    /**
     * 
     * Creates a new host monitor.
     * 
     * @param localController           The local Controller Description
     * @param hostMonitorSettings       The host monitoring settings.
     * @return  the newly created host monitor.
     * @throws HostMonitoringException  The monitoring exception.
     */
    public static HostMonitor newHostMonitor(
            LocalControllerDescription localController,
            HostMonitorSettings hostMonitorSettings) throws HostMonitoringException
    {
        String hostMonitorName = hostMonitorSettings.getName();
        
        HostMonitor hostMonitor = null;
        
        if (hostMonitorName.equals("ganglia"))
        {
            log_.debug("Creating a new Ganglia hostMonitor");
            hostMonitor = new GangliaHostMonitor();
        }
        else
        {
            log_.debug("Creating a new custom hostMonitor ");
            try
            {
                Object hostMonitorObject = PluginUtils.createFromFQN(hostMonitorName);
                hostMonitor = (HostMonitor) hostMonitorObject;
            }
            catch (Exception e)
            {
                log_.error("Unable to instantiate the custom host monitor");
                throw new HostMonitoringException("Unable to load the custom host monitor");
            }
        }

        hostMonitor.setSettings(hostMonitorSettings);
        hostMonitor.setLocalController(localController);
        hostMonitor.initialize();
        return hostMonitor;
    }


    
}
