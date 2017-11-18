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
 * @Transactional no longer plays nice with my @enforce AST Transform. If I have then on the same method I get this error:
 *
 * Unable to produce AST for this phase due to earlier compilation error:
 * startup failed:
 * script1509283741398.groovy: 34: [Static type checking] - The variable [sp] is undeclared.
 *  @ line 34, column 25.
 *       @Enforce({isCreator(sp)})
 *                          ^
 *
 * script1509283741398.groovy: 34: [Static type checking] - Cannot find matching method com.security.TestService#isCreator(java.lang.Object). Please check if the declared type is right and if the method exists.
 * @ line 34, column 15.
 *        @Enforce({isCreator(sp)})
 *
 *  Works in Grails 3.0.17 and 3.2.4, with or without the @CompileStatic.
 *  The only way I can get it to work with Grails 3.3(Gorm 6.1) is to add @CompileDynamic, which I shouldn't have to.
 */
//@Transactional
//@CompileStatic
class TestService{

//@CompileStatic  //compile static conflicts with Enforce in grails 3.3.x
//@CompileDynamic
    @Enforce({isCreator(sp)})
    Sprocket getSprocket(Sprocket sp) {
        return sp
    }
}