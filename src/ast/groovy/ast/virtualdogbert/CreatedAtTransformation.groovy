package ast.virtualdogbert

import jdk.internal.org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

//AST Transformation
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class CreatedAtTransformation implements ASTTransformation {

    public void visit(ASTNode[] astNodes, SourceUnit source) {

        //private final long field creation
        ClassNode myClass = (ClassNode) astNodes[1]
        ClassNode longClass = new ClassNode(Long.class)
        FieldNode field = new FieldNode("timeOfInstantiation", FieldNode.ACC_PRIVATE, longClass, myClass, new ConstantExpression(System.currentTimeMillis()))
        myClass.addField(field)

        //statement
        AstBuilder ab = new AstBuilder()
        List<ASTNode> statement = ab.buildFromCode {
            timeOfInstantiation
        }

        //value of the annotation expression(name of the method)
        def annotationExpression = astNodes[0].members.name
        String annotationValueString = annotationExpression.value

        //public final method creation
        myClass.addMethod(annotationValueString, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, ClassHelper.Long_TYPE, [] as Parameter[], [] as ClassNode[], statement[0])

        //modification of method "add"
        def addMethods = myClass.getMethods("add")
        for(m in addMethods){
            def code = m.getCode().statements

            //statement
            //AstBuilder abc = new AstBuilder()
            Statement s1 = new ExpressionStatement(
                    new BinaryExpression(
                            new VariableExpression('timeOfInstantiation'),
                            Token.newSymbol(org.codehaus.groovy.syntax.Types.EQUAL,0,0),
                            new MethodCallExpression(
                                    new ClassExpression(new ClassNode(java.lang.System)),
                                    'currentTimeMillis',
                                    ArgumentListExpression.EMPTY_ARGUMENTS
                            )
                    )

            )
//            List<ASTNode> statement1 = abc.buildFromString('timeOfInstantiation = System.currentTimeMillis()')
//            List<ASTNode> statement1 = abc.buildFromCode {
//                timeOfInstantiation = System.currentTimeMillis()
//                for(c in code){
//                    c.expression
//                }
//            }

            code.add(0,s1)
            //m.setCode(statement1[0])
        }

        //modification of method "subtract"
        def subtractMethods = myClass.getMethods("subtract")
        for(m in subtractMethods){
            def code = m.getCode().statements

            //statement
            AstBuilder abc = new AstBuilder()
            List<ASTNode> statement1 = abc.buildFromString('timeOfInstantiation = System.currentTimeMillis()')
//            List<ASTNode> statement1 = abc.buildFromCode {
//                timeOfInstantiation = System.currentTimeMillis()
//                for(c in code){
//                    c.expression
//                }
//            }

            code.add(0,statement1[0])
            //m.setCode(statement1[0])
        }

        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(source)

        source.AST.classes.each { ClassNode classNode ->
            scopeVisitor.visitClass(classNode)
        }
    }
}
