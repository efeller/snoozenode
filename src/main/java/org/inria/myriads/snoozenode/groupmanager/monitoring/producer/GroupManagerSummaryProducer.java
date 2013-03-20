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
package org.inria.myriads.snoozenode.groupmanager.monitoring.producer;

import java.io.IOException;
import java.util.ArrayList;

import org.inria.myriads.snoozecommon.communication.NetworkAddress;
import org.inria.myriads.snoozecommon.communication.groupmanager.summary.GroupManagerSummaryInformation;
import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerDescription;
import org.inria.myriads.snoozenode.database.api.GroupManagerRepository;
import org.inria.myriads.snoozenode.groupmanager.estimator.ResourceDemandEstimator;
import org.inria.myriads.snoozenode.groupmanager.monitoring.transport.GroupManagerDataTransporter;
import org.inria.myriads.snoozenode.tcpip.TCPDataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Group manager monitoring data producer.
 * 
 * @author Eugen Feller
 */
public final class GroupManagerSummaryProducer extends TCPDataSender
    implements Runnable
{
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(GroupManagerSummaryProducer.class);
        
    /** Group manager repository reference. */
    private GroupManagerRepository repository_;

    /** Resource demand estimator. */
    private ResourceDemandEstimator estimator_;
    
    /** Lock object. */
    private Object lockObject_;

    /** Monitoring interval. */
    private int monitoringInterval_;
    
    /** Terminated. */
    private boolean isTerminated_;
        
    /**
     * Constructor.
     * 
     * @param repository            The group manager repository
     * @param groupLeaderAddress    The group leader address
     * @param estimator             The estimator
     * @param monitoringInterval    The monitoring interval
     * @throws IOException          The I/O exception
     */
    public GroupManagerSummaryProducer(GroupManagerRepository repository, 
                                       NetworkAddress groupLeaderAddress,
                                       ResourceDemandEstimator estimator,
                                       int monitoringInterval)
        throws  IOException 
    { 
        super(groupLeaderAddress);
        log_.debug("Initializing the group manager summary information producer");
        
        repository_ = repository;
        estimator_ = estimator;
        monitoringInterval_ = monitoringInterval;
        lockObject_ = new Object();
    }
    
    /**
     * Creates the data transporter.
     * 
     * @return    The data transporter
     */
    private GroupManagerDataTransporter createDataTransporter()
    {
        ArrayList<LocalControllerDescription> localControllers = 
            repository_.getLocalControllerDescriptions(estimator_.getNumberOfMonitoringEntries(), false, true);
        ArrayList<String> legacyIpAddresses = repository_.getLegacyIpAddresses();
        GroupManagerSummaryInformation summary = estimator_.generateGroupManagerSummaryInformation(localControllers);
        summary.setLegacyIpAddresses(legacyIpAddresses);
        summary.setLocalControllers(repository_.getLocalControllerDescriptions(0, false, false));
        String groupManagerId = repository_.getGroupManagerId();
        GroupManagerDataTransporter dataTransporter = new GroupManagerDataTransporter(groupManagerId, summary);      
        return dataTransporter;
    }
    
    /** Run method. */
    public void run()
    {    
        try 
        {
            while (!isTerminated_)
            {                
                GroupManagerDataTransporter dataTransporter = createDataTransporter();
                GroupManagerSummaryInformation summary = dataTransporter.getSummary();
                log_.debug(String.format("Sending summary information to the group leader! " +
                                         "Active: %s, passive: %s, requested: %s, used: %s capacity with %d local controllers assigned ", 
                                         summary.getActiveCapacity(), summary.getPassiveCapacity(),
                                         summary.getRequestedCapacity(), summary.getUsedCapacity(),
                                         summary.getLocalControllers().size()) );
                send(dataTransporter);
                synchronized (lockObject_)
                {
                    lockObject_.wait(monitoringInterval_);
                }
            }
        } 
        catch (InterruptedException exception)
        {
            log_.error(String.format("Group manager summary information producer was interruped", exception));
        }
        catch (IOException exception) 
        {
            if (!isTerminated_)
            {
                log_.debug(String.format("I/O error during summary sending (%s). Did the group leader fail?", 
                                         exception.getMessage()));
            }
        } 
        
        close();
        log_.debug("Group manager summary information producer is stopped!");
    }
    
    /** 
     * Terminating the thread.
     */
    public synchronized void terminate() 
    {
        log_.debug("Terminating the group manager summary information producer");
        synchronized (lockObject_)
        {
            isTerminated_ = true;
            lockObject_.notify();
        }
    }
}
