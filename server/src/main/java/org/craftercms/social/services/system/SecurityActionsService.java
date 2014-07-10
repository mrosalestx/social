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

package org.craftercms.social.services.system;

import org.craftercms.social.domain.social.system.SocialSecurityAction;
import org.craftercms.social.exceptions.SocialException;

import java.util.List;

/**
 * SecurityActions Service Definition
 */
public interface SecurityActionsService {

    /**
     * Gets All SecurityAction for the given Tenant.
     * @param tenant Tenant to search for.
     * @return All SecurityActions for the given tenant empty Iterator if nothing if found
     */
    Iterable<SocialSecurityAction> get(final String tenant);

    /**
     * Updates the Roles for the given Action of the Tenant.
     * @param tenant Tenant owner of the Action.
     * @param actionName Action name to update.
     * @param roles New roles to assigned the action.
     * @return the updated SecurityAction,null if unable to find action for the given tenant.
     */
    SocialSecurityAction update(String tenant, String actionName, List<String> roles) throws SocialException;
}
