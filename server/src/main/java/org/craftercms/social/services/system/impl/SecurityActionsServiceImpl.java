/*
 * Copyright (C) 2007-${year} Crafter Software Corporation.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.social.services.system.impl;

import java.util.ArrayList;
import java.util.List;

import org.craftercms.commons.mongo.MongoDataException;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.social.domain.social.system.SocialSecurityAction;
import org.craftercms.social.exceptions.SocialException;
import org.craftercms.social.repositories.security.PermissionRepository;
import org.craftercms.social.security.SocialPermission;
import org.craftercms.social.services.system.SecurityActionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security Actions Service Default implementation.
 */
public class SecurityActionsServiceImpl implements SecurityActionsService {
    private Logger log = LoggerFactory.getLogger(SecurityActionsServiceImpl.class);

    private PermissionRepository permissionRepository;

    @Override
    @HasPermission(type = SocialPermission.class, action = "system.securityActions.read")
    public Iterable<SocialSecurityAction> get(final String context) {
        log.debug("Finding all SecurityActions for {}", context);
        try {
            return permissionRepository.findActions(context);
        } catch (MongoDataException e) {
            log.error("Unable to find all Security actions for given context", e);
            return (Iterable)new ArrayList<SocialSecurityAction>();
        }
    }

    @Override
    @HasPermission(type = SocialPermission.class, action = "system.securityActions.removeWatcher")
    public SocialSecurityAction update(final String context, final String actionName,
                                 final List<String> roles) throws SocialException {
        log.debug("Updating Roles for {} of context {} to {}", actionName, context, roles);
        try {
            if (actionName.toLowerCase().startsWith("system.")) {
                throw new IllegalArgumentException("System Actions can't be changed");
            }
            return permissionRepository.updateSecurityAction(context, actionName, roles);
        } catch (MongoDataException ex) {
            log.error("Unable to Update Security Action", ex);
            throw new SocialException("Unable to removeWatcher Security Action", ex);
        }
    }

    @Override
    public void save(final SocialSecurityAction action) throws SocialException {
        log.debug("Creating new Action {}",action);
        try{
            permissionRepository.save(action);
        }catch (MongoDataException ex){
            log.error("Unable to save new action", ex);
            throw new SocialException("Unable to save new Action");
        }
    }

    public void setPermissionRepositoryImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }
}
