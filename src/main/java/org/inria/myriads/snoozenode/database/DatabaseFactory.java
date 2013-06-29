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
package org.inria.myriads.snoozenode.database;

import org.inria.myriads.snoozecommon.communication.groupmanager.GroupManagerDescription;
import org.inria.myriads.snoozenode.configurator.monitoring.external.ExternalNotifierSettings;
import org.inria.myriads.snoozenode.database.api.GroupLeaderRepository;
import org.inria.myriads.snoozenode.database.api.GroupManagerRepository;
import org.inria.myriads.snoozenode.database.api.LocalControllerRepository;
import org.inria.myriads.snoozenode.database.api.impl.GroupLeaderMemoryRepository;
import org.inria.myriads.snoozenode.database.api.impl.GroupManagerMemoryRepository;
import org.inria.myriads.snoozenode.database.api.impl.LocalControllerMemoryRepository;
import org.inria.myriads.snoozenode.database.enums.DatabaseType;
import org.inria.myriads.snoozenode.groupmanager.leaderpolicies.GroupLeaderPolicyFactory;
import org.inria.snoozenode.external.notifier.ExternalNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database factory.
 * 
 * @author Eugen Feller
 */
public final class DatabaseFactory 
{
    /** Define the logger. */
    private static final Logger log_ = LoggerFactory.getLogger(GroupLeaderPolicyFactory.class);
    
    /** Hide constructor. */
    private DatabaseFactory()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns the group leader repository.
     * @param groupLeaderDescription 
     * 
     * @param virtualMachineSubnets    The virtual machine subnets
     * @param maxCapacity             The maximum capacity
     * @param type                    The database type
     * @return                        The group leader repository
     */
    public static GroupLeaderRepository newGroupLeaderRepository(GroupManagerDescription groupLeaderDescription, String[] virtualMachineSubnets,   
                                                                 int maxCapacity,
                                                                 DatabaseType type,
                                                                 ExternalNotifier externalNotifier) 
    {
        GroupLeaderRepository repository = null;
        switch (type) 
        {
            case memory :       
                repository = new GroupLeaderMemoryRepository(groupLeaderDescription, virtualMachineSubnets, maxCapacity, externalNotifier);        
                break;
                       
            default:
                log_.error("Unknown group leader database type selected");
        }
        
        return repository;
    }

    /**
     * Returns the group manager repository.
     * 
     * @param groupManagerId    The group manager identifier
     * @param maxCapacity       The maximum capacity
     * @param type              The database type
     * @return                  The group manager repository
     */
    public static GroupManagerRepository newGroupManagerRepository(String groupManagerId, 
                                                                   int maxCapacity,
                                                                   DatabaseType type,
                                                                   ExternalNotifierSettings externalNotifierSettings,
                                                                   ExternalNotifier externalNotifier
                                                                    ) 
    {
        GroupManagerRepository repository = null;
        switch (type) 
        {
            case memory :       
                repository = new GroupManagerMemoryRepository(groupManagerId, maxCapacity, externalNotifierSettings, externalNotifier);
                break;
                       
            default:
                log_.error("Unknown group manager database type selected");
        }
        return repository;
    }

    /**
     * Returns the local controller repository.
     * 
     * @param type       The database type
     * @return           The local controller repository
     */
    public static LocalControllerRepository newLocalControllerRepository(DatabaseType type, 
            ExternalNotifier externalNotifier) 
    {
        LocalControllerRepository repository = null;
        switch (type) 
        {
            case memory :       
                repository = new LocalControllerMemoryRepository(externalNotifier);
                break;
                       
            default:
                log_.error("Unknown local controller database type selected");
        }
        return repository;
    }
}
