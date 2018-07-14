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

import ast.virtualdogbert.Params
import grails.gorm.transactions.Transactional
import org.springframework.transaction.annotation.Isolation

/**
 * The EnforcerService has one enforce method for enforcing business rules, and is extended by the traits it implements.
 */
@Transactional
class TestService implements RoleTrait, DomainRoleTrait, CreatorTrait {

    @Transactional
    //@Enforce({isCreator(sprocket)})
    Sprocket getSprocket(Sprocket sprocket) {
        return sprocket
    }

    @Params(
            value = 'something',
            success = {false},
            isolation = Isolation.DEFAULT,
            timeout = -1,
            readOnly = false,
            emptyList = [],
            listOfString = ["one", "two", "three"],
            listOfLong = [1L, 2L, 3L]
    )
    def testParamsAnnotation(){

    }
}
