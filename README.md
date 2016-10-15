[![Travis Badge](https://secure.travis-ci.org/dattack/standalone-jndi.svg?branch=master)](https://travis-ci.org/dattack/standalone-jndi/builds)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ebbfe656384f4f1993ec46fffd1d8aa3)](https://www.codacy.com/app/dattack/standalone-jndi)
[ ![Codeship Badge](https://app.codeship.com/projects/2b7c4b00-748f-0134-45cb-12948b47b8fd/status?branch=master)](https://app.codeship.com/projects/179241)
[![CircleCI](https://circleci.com/gh/dattack/standalone-jndi.svg?style=svg)](https://circleci.com/gh/dattack/standalone-jndi)
[![codecov](https://codecov.io/gh/dattack/standalone-jndi/branch/master/graph/badge.svg)](https://codecov.io/gh/dattack/standalone-jndi)
[![license](https://img.shields.io/:license-Apache-blue.svg?style=plastic-square)](LICENSE.md)

Standalone JNDI
=======

A JNDI implementation to use with your standalone applications.

Where can i get the latest release?
=========

You can pull it from the Dattack maven repository:

```xml
  <repositories>
    <repository>
      <id>public-dattack-releases</id>
      <name>Dattack Releases</name>
      <url>http://maven.dattack.com/release</url>
      <releases>
        <updatePolicy>never</updatePolicy>
        <checksumPolicy>fail</checksumPolicy>
      </releases>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
    </repository>
    <repository>
      <id>public-dattack-snapshots</id>
      <name>Dattack Snapshots</name>
      <url>http://maven.dattack.com/snapshot</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <updatePolicy>always</updatePolicy>
        <checksumPolicy>warn</checksumPolicy>
      </snapshots>
    </repository>
  </repositories>
```

Try it out
=========

If you use Maven, include this dependency in your pom.xml:

```xml
  <dependency>
    <groupId>com.dattack</groupId>
    <artifactId>standalone-jndi</artifactId>
    <version>x.y.z</version>
  </dependency>
```

The source code on the master branch is the current state of development; it is
not recommended for general use. If you prefer to build from source, please use
an appropriate release tag.

Bugs and Feedback
=========
For bugs and discussions please use the [Github Issues](https://github.com/dattack/standalone-jndi/issues). 
If you have other questions, please contact by [email](mailto:dev@dattack.com) 
or [@dattackteam](https://twitter.com/dattackteam) 


Copyright and license
=========
Copyright 2016 Dattack Team

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this work except in compliance with the License. You may obtain a copy of the 
License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.

This product includes software developed by The Apache Software Foundation (http://www.apache.org/).
