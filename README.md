Step-by-step how to build android test projects is written by scala
=====

This repository is example and explanation of building android test projects.

There is already known a method. It is packaging both application project and test project to one apk. for details [Building Android Test Projects](https://github.com/jberkel/android-plugin/wiki/Building-Android-Test-Projects).

But above method has a issue. From perspective of completely, both projects should be separated. Because release apk don't include classes for tests. Testing should be done on same conditions.

A method is illustrated in this repository resolve it. projects are separated.

Diffs in commits are just example. Completely sbt settings is more complex.

Note (or Issues)
-----

As mentioned above, this repository is only example. Some settings and processes are ommitted for explanation simplicity.

For Instance, correctly handling libraries, modification detecting before generating optimal jar, and more somthing processes for efficiency of building.

Final goal, adding honest support for android test project (and android library project, android library-test project) in android-plugin. I want to believe :->
