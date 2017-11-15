Groovy Compile-time Meta-programming Magic's Biggest Secrets Finally Revealed!
----

Last time I gave a talk I showed you, that Meta programming was just another trick, and I showed you how it worked. Tonight I'm going to try to go once step beyond and show you the tricks behind compile-time meta-programming. So that once again you'll see it as another way of programming, rather than magic. However compile-time meta programing, is not for the faint of heart. It's used mainly by the maintainers of Groovy, Groovy related frameworks, and plugin/library designers. I've writen about a dozzen AST Transforms, and I don't consider myself an expert, and you may very well, have questions that I can't answer, however I'm going to share with you my expirence.

AST Transforms Advantages
* No run-time performance penalties like runtime-meta programming
* Can help simplify code
* Remove boiler plate code
* Reduce errors by providing a proven implementation
    
AST Transform Disadvantages
* Documentation can be a bit lacking in the api
* It can be hard to get help
* A little bit harder to Debug(I'll show some workarounds)
* More verbose that writing actual code
* Harder to maintain
* Can conflict with other AST transforms

What is an ast transform?
----

* http://docs.groovy-lang.org/latest/html/documentation/#_compile_time_metaprogramming
* http://docs.groovy-lang.org/latest/html/gapi/org/codehaus/groovy/ast/tools/package-summary.html
* http://docs.grails.org/latest/api/org/grails/compiler/injection/GrailsASTUtils.html
* http://www.sdidit.nl/2013/01/groovy-ast-writing-annotation.html


When you write code you have a bunch of files just containing text, When you use the Groovy Compiler the compiler processes that text using a parser Called Antlr, converting the text to an abstract syntax tree. The Groovy compiler has several phases where that abstract syntax tree is transformed, untill it becomes it's final byte code output.

The Groovy compiler allows you to intervene in the phases of the compilation, which is where local and global AST transforms come in, allowing you to change the syntax tree, which will change the code that is compiled.

Compilation phases:
----
* Initialization: source files are opened and environment configured
* Parsing: the grammar is used to to produce tree of tokens representing the source code
* Conversion: An abstract syntax tree (AST) is created from token trees.
* Semantic Analysis: Performs consistency and validity checks that the grammar can’t check for, and resolves classes.
* Canonicalization: Complete building the AST
* Instruction Selection: instruction set is chosen, for example Java 6 or Java 7 bytecode level
* Class Generation: creates the bytecode of the class in memory
* Output: write the binary output to the file system
* Finalization: Perform any last cleanup
    
Local AST Transforms are applied by using annotations, while Global AST transforms are applied to all classes being compiled.

Steps to writing an AST Transform.
 ----
* Write the code you think you want your AST Transform to perform.
* Take note of the inputs/outputs
* Can some of the code be delegated to through a method call(the less AST you write the less you have to maintain)
* Look at that code in the Groovy Console/Groovy AST Browser
* Write Transform
* Debug Transform
* ???
* Profit? 

Lets Take a look at and AST Transform(Config, adding a property and a method)
----

* visitor pattern
    * parameter
        * String
        * Closure
        
    * Target types
        * type
        * method
        * field
        
    * Nodes
    * Statements
    * Expressions
    
    * groovyConsole
    
    * debugging
        in build.gradle add:
        
        compileGroovy.groovyOptions.forkOptions.jvmArgs  = ['-Xdebug', '-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005']
        compileTestGroovy.groovyOptions.forkOptions.jvmArgs = ['-Xdebug', '-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005']

* Another AST Transform(Enforce Adding calls in methods)
    * Closures as parameters
    * VariableScopeVisitor(parameter access)
* Another AST Transform(Reinforce, ReinforceFilter)
* Another AST Transform(ErrorsHandler, SkipErrorsHandler)
* Global AST transform(Command example)

GDSL
----
* http://www.tothenew.com/blog/gdsl-awesomeness-introduction-to-gdsl-in-intellij-idea/
* https://confluence.jetbrains.com/display/GRVY/Scripting+IDE+for+DSL+awareness 
* https://intellij-support.jetbrains.com/hc/en-us/community/posts/203366410-Can-I-use-GDSL-with-Groovy-AST-transforms-that-have-closures-as-parameters-

Macro methods(future?)
----
* http://docs.groovy-lang.org/next/html/documentation/#_macros