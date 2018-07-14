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
 *
 * Some of the setup is derived from the following grails plugings(Appache Licence)
 * https://github.com/groovy/groovy-core/blob/4993b10737881b2491c2daa01526fb15dd889ac5/src/main/org/codehaus/groovy/transform/NewifyASTTransformation.java
 * https://github.com/grails-plugins/grails-redis/tree/master/src/main/groovy/grails/plugins/redis
 */

package ast.virtualdogbert

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ParamsASTTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {

        ClassNode beforeNode = new ClassNode(Params.class)

        MethodNode methodNode = (MethodNode) nodes[1]


        AnnotationNode annotationNode = methodNode.getAnnotations(beforeNode)[0]
        String configPath = annotationNode?.getMember('value')?.getText()
        annotationNode?.getMember('fixed')

        Params.getDeclaredMethod('value').defaultValue //string
        Params.getDeclaredMethod('success').defaultValue.class.name // class doesn't seem to work very well
        Params.getDeclaredMethod('isolation').defaultValue //enum
        Params.getDeclaredMethod('timeout').defaultValue //-1
        Params.getDeclaredMethod('readOnly').defaultValue //boolean
        Params.getDeclaredMethod('emptyList').defaultValue // empty array of string
        Params.getDeclaredMethod('listOfString').defaultValue //array of strings ['one','two','three']
        Params.getDeclaredMethod('listOfLong').defaultValue // array of longs [1,2,3]

        annotationNode?.getMember('success')// closure expression
        annotationNode?.getMember('success').text//{ -> ... }
        annotationNode?.getMember('success').type.name//groovy.lang.Closure
        //annotationNode?.getMember('success').value//breaks

        annotationNode?.getMember('value') //constant expression
        annotationNode?.getMember('value').getType().name //class name
        annotationNode?.getMember('value').getText() // string


        annotationNode?.getMember('isolation') //property expression
        annotationNode?.getMember('isolation').getText() //org.springframework.transaction.annotation.Isolation.DEFAULT
        annotationNode?.getMember('isolation').getType().name //java.lang.Object
        //annotationNode?.getMember('isolation').value //breaks


        annotationNode?.getMember('timeout') // constant expression
        annotationNode?.getMember('timeout').getText() //"-1"
        annotationNode?.getMember('timeout').getType().name //
        ((ConstantExpression)annotationNode?.getMember('timeout')).value // -1

        annotationNode?.getMember('readOnly') //
        annotationNode?.getMember('readOnly').getText() //string false
        annotationNode?.getMember('readOnly').getType().name //boolean
        ((ConstantExpression)annotationNode?.getMember('readOnly')).value //false

        annotationNode?.getMember('emptyList') // list expression
        annotationNode?.getMember('emptyList').getText() // "[]"
        annotationNode?.getMember('emptyList').getType().name //java.util.List
        //annotationNode?.getMember('emptyList').value // breaks

        annotationNode?.getMember('listOfString') //expression
        annotationNode?.getMember('listOfString').getText() //[one, two, three]
        annotationNode?.getMember('listOfString').getType().name //java.util.List
        //annotationNode?.getMember('listOfString').value // breaks

        annotationNode?.getMember('listOfLong') //expression
        annotationNode?.getMember('listOfLong').getText() //[1, 2, 3]
        annotationNode?.getMember('listOfLong').getType().name //java.util.List
        //annotationNode?.getMember('listOfLong').value //breaks
    }
}
