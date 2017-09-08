
AST Transform Test Project
-------------------------------

This is just a project I use for testing/debugging AST Transforms. It has source sets, setup so that
AST transforms will be compiled first, and then applied when compiling the rest of the code.

I've added runConfig, with intellij run configs, that can be copied to the .idea/runConfigurations 
directory once created. If you don't see that directory, create a run config, edit it and click on 
the share check box.

Also in the build.gradle are lines for remote debugging. Comment those lines in, and run a bootRun or a 
test, this will start a remote debug session, then use the remote debug run config to start debugging. 
This will allow you to debug AST transform, as they are being applied to code.