# Kalahluhlahley

Java bot to play the game of Kalah using the _Kalah Game Engine_ included.

## How to build our MKAgent

to build the agent simply use the command:

`ant jar`

This will create a Java Executable that you can then run against the reference agent with:

`java -jar ManKalah.jar "java -jar build/src/jar/MKAgent.jar" "java -jar MKRefAgent.jar"`

If this is a pain in the arse for you then simply use the **BuildAndRunMKAgent** shell script in the root with:

`./BuildAndRunMKAgent`

It'll do all the good guff and maybe even play the game properly eventually.

## Coding Policy

Only one. Since we don't have continous integration for this tiny project we should instead settle for simply **always run the build script before you submit!**. This will pick up any compiler errors/test failures.

Also try to do any changes in a branch and then create a pull request to merge the changes in. This'll stop the build getting inundated with loads of changes in the last week. When you want to make a change first create a new branch with

`git checkout -b jake-terrible-code`

then use `git checkout jake-terrible-code` to hop on that branch. Git add, commit, push as normal and then when it comes time to integrate simply swap to your branch on github and press the _new pull request_ button. Whap someone on to review and then wait for approval before merging. 
