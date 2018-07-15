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
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
/**
 * The annotation injects a call to either the default handler(@see com.virtualdogbert.ControllerEnhancer.#errorsHandler())
 * or one specified by name using the handler parameter.
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ErrorsHandlerASTTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes.length != 2) return
        ClassNode beforeNode = new ClassNode(ErrorsHandler.class)

        if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof MethodNode) {

            MethodNode methodNode = (MethodNode) nodes[1]
            AnnotationNode annotationNode = methodNode.getAnnotations(beforeNode)[0]
            String handlerName = annotationNode?.getMember('handler')?.getText()
            ListExpression params = new ListExpression(getParamsList(methodNode.parameters.toList()))
            applyToMethod(methodNode, sourceUnit, handlerName, params)

        } else if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof ClassNode) {

            ClassNode classNode = (ClassNode) nodes[1]
            AnnotationNode annotationNode = classNode.getAnnotations(beforeNode)[0]
            String handlerName = annotationNode?.getMember('handler')?.getText()

            classNode.methods.each { MethodNode methodNode ->
                ListExpression params = new ListExpression(getParamsList(methodNode.parameters.toList()))
                applyToMethod(methodNode, sourceUnit, handlerName, params, true)
            }

        }
    }

    /**
     * This applies the error handing logic to a method node.
     *
     * @param methodNode The method node to inject the enforce logic
     * @param sourceUnit the source unit which is used for fixing the variable scope
     * @param params the parameter passed into the annotation at the class or method level
     * @param fromClass If the annotation comes from the class level
     */
    private void applyToMethod(MethodNode methodNode, SourceUnit sourceUnit, String handler, ListExpression params, boolean fromClass = false) {
        if(methodNode.private){
            return
        }

        ClassNode ErrorHandlerNode = new ClassNode(ErrorsHandler.class)
        ClassNode SkipErrorsHandlerNode = new ClassNode(SkipErrorsHandler.class)
        if (fromClass &&
                (methodNode.getAnnotations(ErrorHandlerNode)[0] || methodNode.getAnnotations(SkipErrorsHandlerNode)[0])) {
            return
            //If the annotation is from the class level, but the method node has it's own @ErrorsHandler annotation don't apply the class level logic.
        }

        BlockStatement methodBody = (BlockStatement) methodNode.getCode()
        List statements = methodBody.getStatements()
        statements.add(0, createErrorsHandlerCall(handler, params))

        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(sourceUnit)
        sourceUnit.AST.classes.each { ClassNode classNode ->
            scopeVisitor.visitClass(classNode)
        }
    }

    /**
     *  Collects the params from the method, that are of the command artefact type.
     *
     * @param members The map of members / parameters.
     *
     * @return A list of parameters that will be passed to the error handler.
     */
    private List<Expression> getParamsList(List<Parameter> methodParams) {
        List<Expression> parameterExpressionList = []
        methodParams.each { Parameter parameter ->
            if (parameter.type.name.endsWith('Command')) {
                parameterExpressionList << (new VariableExpression(parameter))
            }
        }

        parameterExpressionList << getControllerErrors()

        return parameterExpressionList
    }

    private MapExpression getControllerErrors(){
        Expression thisExpression = new VariableExpression("this")
        Expression errorsCall = new MethodCallExpression(thisExpression, 'getErrors', new ArgumentListExpression())
        Expression hasErrorsCall = new MethodCallExpression(thisExpression, 'hasErrors', new ArgumentListExpression())
        Parameter[] parameters = [] as Parameter[]
        ClosureExpression hasErrorsClosure = new ClosureExpression(parameters, new ReturnStatement(hasErrorsCall))
        MapExpression controllerErrorsMap = new MapExpression()
        controllerErrorsMap.addMapEntryExpression(new MapEntryExpression(new ConstantExpression('hasErrors'), hasErrorsClosure))
        controllerErrorsMap.addMapEntryExpression(new MapEntryExpression(new ConstantExpression('errors'), errorsCall))
        return controllerErrorsMap
    }

    /**
     *  Creates the call to the error handler, either the default injected, or the one passed in the handler parameter,
     *  using the list of parameters generated from the get ParamsList
     *
     * @param params A list of command artefact parameters that will be passed to the error handler.
     *
     * @return the statement created for injecting the call to the error handler
     */
    private Statement createErrorsHandlerCall(String handler, ListExpression params) {
        Expression thisExpression = new VariableExpression("this")
        Expression call = new MethodCallExpression(thisExpression, handler ?: 'errorsHandler', new ArgumentListExpression(params))
        BooleanExpression booleanExpression = new BooleanExpression(call)
        return new IfStatement(booleanExpression, new ReturnStatement(new ConstantExpression(null)), new EmptyStatement())
    }
}
