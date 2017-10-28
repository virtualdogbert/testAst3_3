/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.security

import ast.virtualdogbert.Enforce
/**
 * This trait is for the EnforcerService, extending it's capability to enforcing domain roles, without the verbosity of calling a service.
 */
trait DomainRoleTrait {

    /**
         *  This method will check if a user(defaulting to the currently logged in user, has a DomainRole on an object.
         *
         *  @param  role the role to check to see if the user has on the domainObject
         *  @param domainObject the instance of the domain object to check if the user has a DomainRole on
         *  @parm user the user to check if  it has the role on the domain object, defaults to null which is swapped for springSecurityService.currentUser
         *  @return true if the user has the DomainRole or the DomainRole fall in to the following hierarchy and false other wise:
         *  Map roleHierarchy = [
         *            owner : ['owner', 'editor', 'viewer'],
         *            editor: ['editor', 'viewer'],
         *            viewer: ['viewer']
         *  ]
         */
    boolean hasDomainRole(String role, domainObject, User user = null) {
        if (!user) {
            user = springSecurityService.currentUser
        }

        String domainName =  domainObject.getClass().name

        Map roleHierarchy = [
                owner : ['owner', 'editor', 'viewer'],
                editor: ['editor', 'viewer'],
                viewer: ['viewer']
        ]

        DomainRole domainRole = DomainRole.findByRoleAndDomainNameAndDomainIdAndUser(role, domainName, domainObject.id, user)
        domainRole?.role in roleHierarchy[role]
    }

//    /**
//         * This method checks the domain object to see if it has a reference to a user(passed in or defaulted to springSecurityService.currentUser)
//         * This makes it so that the original creator of an object can add permissions to that object.
//         *
//         * @param domainObject The domain object to check for a user reference domainObject.creator
//         * @param user  the user(defaulted to springSecurityService.currentUser) to compare to domainObject.creator
//         * @return true if the user is the same as the creator user reference, false otherwise
//         */
//    Boolean isCreator(domainObject, user = null) {
//        if (!user) {
//            user = springSecurityService.currentUser
//        }
//
//        domainObject.creator.id == user.id
//    }

    /**
         * This method changes the DomainRole of a domainObject for a user, and creates one if one doesn't exist.
         *
         * @param role the role to set for the domainObject
         * @param domainObject the domain object to set a role for
         * @param user the user to set the DomainRole for defaulting to springSecurityService.currentUser
         */
    @Enforce({ hasDomainRole('owner', domainObject) || isCreator(domainObject) || haRole('ROLE_ADMIN') })
    void changeDomainRole(String role, domainObject, User user = null) {
        if (!user) {
            user = springSecurityService.currentUser
        }

        String domainName =  domainObject.getClass().name

        DomainRole domainRole = DomainRole.findByRoleAndDomainNameAndDomainIdAndUser(role, domainName, domainObject.id, user)

        if (domainRole) {
            domainRole.role = role
        } else {
            domainRole = new DomainRole(role: role, domainName: domainName, domainId: domainObject.id, user: user, creator: springSecurityService.getCurrentUser())
        }

        domainRole.save(failOnError: true)
    }

    /**
         * This method removes a DomainRole from an  domainObject
         *
         * @param domainObject the domainObject to remove the role from
         * @param user the use for which the role is being removed.
         */
    @Enforce({ hasDomainRole('owner', domainObject) || haRole('ROLE_ADMIN') })
    void removeDomainRole(domainObject, User user = null) {
        if (!user) {
            user = springSecurityService.currentUser
        }

        String domainName =  domainObject.getClass().name

        DomainRole domainRole = DomainRole.findByRoleAndDomainNameAndDomainIdAndUser(role, domainName, domainObject.id, user)

        domainRole?.delete()
    }
}