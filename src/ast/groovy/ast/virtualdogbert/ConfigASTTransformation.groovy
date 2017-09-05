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

import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.springframework.beans.factory.annotation.Value

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ConfigASTTransformation extends AbstractASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes.length != 2) return
        ClassNode beforeNode = new ClassNode(Config.class)
        Expression defaultValue = null
        FieldNode fieldNode = (FieldNode) nodes[1]

        if (fieldNode.hasInitialExpression()) {
            defaultValue = fieldNode.initialValueExpression
        }

        AnnotationNode annotationNode = fieldNode.getAnnotations(beforeNode)[0]
        String configPath = annotationNode?.getMember('value')?.getText()
        addGrailsApplication(fieldNode)

        if (((ConstantExpression) annotationNode?.getMember('stat'))?.value) {
            addAnnotation(fieldNode, configPath, defaultValue)
        } else {
            addGetConfig(fieldNode, configPath, defaultValue)
            fieldNode.declaringClass.removeField(fieldNode.name)
        }

    }

    private void addAnnotation(FieldNode fieldNode, String configPath, Expression defaultValue = null) {
        AnnotationNode annotation = new AnnotationNode(new ClassNode(Value.class))

        if (defaultValue) {
            annotation.addMember('value', (Expression) (new ConstantExpression("\${$configPath:$defaultValue.text}".toString())))
        } else {
            annotation.addMember('value', (Expression) (new ConstantExpression("\${$configPath}".toString())))
        }

        fieldNode.addAnnotation(annotation)
    }


    private void addGetConfig(FieldNode fieldNode, String configPath, Expression defaultValue = null) {
        ClassNode classNode = fieldNode.type
        String name = "get${fieldNode.name[0].toUpperCase()}${fieldNode.name[1..-1]}"

        ClassNode parent = fieldNode.declaringClass
        parent.addMethod(new MethodNode(name, ACC_PUBLIC, classNode, [] as Parameter[], [] as ClassNode[], createGetConfig(configPath, classNode, defaultValue)))
    }


    private void addGrailsApplication(FieldNode fieldNode) {
        ClassNode parent = fieldNode.declaringClass

        if (!parent.properties*.name.contains('grailsApplication')) {
            ClassNode grailsApplicationClass = new ClassNode(GrailsApplication.class)
            PropertyNode property = new PropertyNode('grailsApplication', ACC_PUBLIC, grailsApplicationClass, parent, null, null, null)
            parent.addProperty(property)
        }
    }

    private Statement createGetConfig(String configPath, ClassNode classType, Expression defaultValue = null) {
        Expression thisExpression = new VariableExpression("this")
        Expression service = new MethodCallExpression(thisExpression, 'getGrailsApplication', new ArgumentListExpression())
        Expression config = new MethodCallExpression(service, 'getConfig', new ArgumentListExpression())
        ArgumentListExpression arguments = new ArgumentListExpression()
        arguments.addExpression((Expression) (new ConstantExpression(configPath)))
        arguments.addExpression((Expression) (new ClassExpression(classType)))

        if(defaultValue) {
            arguments.addExpression(defaultValue)
        }
        Expression getProperty = new MethodCallExpression(config, 'getProperty', arguments)

        return new ExpressionStatement(getProperty)
    }
}
