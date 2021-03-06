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

package org.craftercms.social.controllers.rest.v3.system;

import com.wordnik.swagger.annotations.Api;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.profile.api.Profile;
import org.craftercms.security.exception.AuthenticationRequiredException;
import org.craftercms.social.domain.social.system.SocialContext;
import org.craftercms.social.exceptions.SocialException;
import org.craftercms.social.security.SecurityActionNames;
import org.craftercms.social.security.SocialSecurityUtils;
import org.craftercms.social.services.system.ContextPreferencesService;
import org.craftercms.social.services.system.SocialContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@RequestMapping("/api/3/system/context")
@Controller
@Api(value = "Handles Context Configuration", description = "Creates and associates Social " +
    "Context to profiles")
public class SocialContextController {

    @Autowired
    private SocialContextService socialContextService;
    @Autowired
    private ContextPreferencesService contextPreferencesService;


    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    public Iterable<SocialContext> getAllContexts() throws SocialException {
        return socialContextService.getAllContexts();
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public SocialContext create(@RequestParam final String contextName) throws SocialException {
        return socialContextService.createNewContext(contextName);
    }

    @RequestMapping(value = "/{id}/{profileId}", method = RequestMethod.POST)
    @ResponseBody
    public Profile addProfileToContext(@PathVariable("id") final String contextId, @PathVariable("profileId") final
    String profileId, @RequestParam final String roles) throws SocialException {
        if (roles.toUpperCase().contains(SecurityActionNames.ROLE_SOCIAL_SUPERADMIN)) {
            throw new IllegalArgumentException(SecurityActionNames.ROLE_SOCIAL_SUPERADMIN + " is not a valid role");
        }

        return socialContextService.addProfileToContext(profileId, contextId, StringUtils.split(roles, ','));
    }

    @RequestMapping(value = "/{id}/{profileId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Profile removeProfileFromContext(@PathVariable("id") final String contextId, @PathVariable("profileId")
    final String profileId) throws SocialException {
        return socialContextService.removeProfileFromContext(contextId, profileId);
    }

    @RequestMapping(value = "/preferences/email" ,method = {RequestMethod.PUT,RequestMethod.POST})
    @ResponseBody
    public boolean saveEmailTemplate(@RequestParam(required = true) final String template,@RequestParam(required =
        true) final String type) throws SocialException {
        if(!checkIfUserIsAdmin()){
            throw  new AuthenticationRequiredException("User must be logged in and must be social admin or context admin");
        }

        if(StringUtils.isBlank(template)){
            throw new IllegalArgumentException("\"template\" param is cannot be blank");
        }
        if(StringUtils.isBlank(type) || !Arrays.asList("DAILY","WEEKLY","INSTANT").contains(type.toUpperCase())){
            throw new IllegalArgumentException("\"type\" param can not be blank and must be on of the following "
                + "values DAILY,WEEKLY,INSTANT");
        }
        return contextPreferencesService.saveEmailTemplate(SocialSecurityUtils.getContext(), type.toUpperCase(),
            template);
    }


    @RequestMapping(value = "/preferences/email" ,method = RequestMethod.GET)
    @ResponseBody
    public String getSaveEmailTemplate(@RequestParam(required =
        true) final String type) throws SocialException {
        if(!checkIfUserIsAdmin()){
            throw  new AuthenticationRequiredException("User must be logged in and must be social admin or context admin");
        }
        if(StringUtils.isBlank(type) || !Arrays.asList("DAILY","WEEKLY","INSTANT").contains(type.toUpperCase())){
            throw new IllegalArgumentException("\"type\" param can not be blank and must be on of the following "
                + "values DAILY,WEEKLY,INSTANT");
        }
        return contextPreferencesService.getEmailTemplate(SocialSecurityUtils.getContext(),type.toUpperCase());
    }


    @RequestMapping(value = "/preferences", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getContextPreference() {
        if (!SocialSecurityUtils.getCurrentProfile().getUsername().equalsIgnoreCase(SocialSecurityUtils.ANONYMOUS)) {
            return contextPreferencesService.getContextPreferences(SocialSecurityUtils.getContext());
        }
        throw  new AuthenticationRequiredException("User must be logged in");
    }

    @RequestMapping(value = "/updatePreference", method = RequestMethod.POST)
    @ResponseBody
    public boolean savePreferences(@RequestParam final Map<String, Object> preferences) {
        if (!SocialSecurityUtils.getCurrentProfile().getUsername().equalsIgnoreCase(SocialSecurityUtils.ANONYMOUS)) {
            if (SocialSecurityUtils.getCurrentProfile().hasRole(SecurityActionNames.ROLE_SOCIAL_ADMIN) ||
                SocialSecurityUtils.getCurrentProfile().hasRole(SecurityActionNames.ROLE_SOCIAL_SUPERADMIN)) {
                return contextPreferencesService.saveContextPreference(SocialSecurityUtils.getContext(), preferences);
            }
        }
        throw  new AuthenticationRequiredException("User must be logged in and must be social admin or context admin");
    }



    private boolean checkIfUserIsAdmin(){
        return !SocialSecurityUtils.getCurrentProfile().getUsername().equalsIgnoreCase(SocialSecurityUtils.ANONYMOUS)
            && SocialSecurityUtils.getCurrentProfile().hasRole(SecurityActionNames.ROLE_SOCIAL_ADMIN) ||
            SocialSecurityUtils.getCurrentProfile().hasRole(SecurityActionNames.ROLE_SOCIAL_SUPERADMIN);
    }
}
