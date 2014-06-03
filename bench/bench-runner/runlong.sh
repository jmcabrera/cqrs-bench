#!/bin/bash
repo="D:\mvn.repo"
java \
-Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false \
-Xmx1g -Xms1g \
-classpath "target\classes\;$repo\commons-cli\commons-cli\1.2\commons-cli-1.2.jar;$repo\io\cqrs\bench\api\0.0.1-SNAPSHOT\api-0.0.1-SNAPSHOT.jar;$repo\io\cqrs\bench\cqrs-manual\0.0.1-SNAPSHOT\cqrs-manual-0.0.1-SNAPSHOT.jar;$repo\io\cqrs\bench\akka\0.0.1-SNAPSHOT\akka-0.0.1-SNAPSHOT.jar;$repo\org\scala-lang\scala-library\2.10.4\scala-library-2.10.4.jar;$repo\com\typesafe\akka\akka-actor_2.10\2.3.3\akka-actor_2.10-2.3.3.jar;$repo\com\typesafe\config\1.2.1\config-1.2.1.jar;$repo\com\typesafe\akka\akka-slf4j_2.10\2.3.3\akka-slf4j_2.10-2.3.3.jar;$repo\org\slf4j\slf4j-log4j12\1.7.5\slf4j-log4j12-1.7.5.jar;$repo\log4j\log4j\1.2.15\log4j-1.2.15.jar;$repo\org\slf4j\slf4j-api\1.7.5\slf4j-api-1.7.5.jar" \
io.cqrs.bench.runner.BencherTest -c 500000 -a 5000000 -t 8 
