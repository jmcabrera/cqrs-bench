#!/bin/bash
repo="D:\mvn.repo"
java \
-Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false \
-Xmx1g -Xms1g \
-classpath "target\classes\;$repo\commons-cli\commons-cli\1.2\commons-cli-1.2.jar;$repo\io\cqrs\bench\api\0.0.1-SNAPSHOT\api-0.0.1-SNAPSHOT.jar;$repo\io\cqrs\bench\cqrs-manual\0.0.1-SNAPSHOT\cqrs-manual-0.0.1-SNAPSHOT.jar;$repo\io\cqrs\bench\akka\0.0.1-SNAPSHOT\akka-0.0.1-SNAPSHOT.jar;$repo\org\scala-lang\scala-library\2.10.4\scala-library-2.10.4.jar;$repo\com\typesafe\akka\akka-actor_2.10\2.3.3\akka-actor_2.10-2.3.3.jar;$repo\com\typesafe\config\1.2.1\config-1.2.1.jar;$repo\com\typesafe\akka\akka-slf4j_2.10\2.3.3\akka-slf4j_2.10-2.3.3.jar;$repo\io\cqrs\bench\jpa\0.0.1-SNAPSHOT\jpa-0.0.1-SNAPSHOT.jar;$repo\org\apache\openejb\openejb-core\4.6.0\openejb-core-4.6.0.jar;$repo\org\apache\openejb\mbean-annotation-api\4.6.0\mbean-annotation-api-4.6.0.jar;$repo\org\apache\openejb\openejb-jpa-integration\4.6.0\openejb-jpa-integration-4.6.0.jar;$repo\org\apache\openejb\javaee-api\6.0-5\javaee-api-6.0-5.jar;$repo\org\apache\commons\commons-lang3\3.1\commons-lang3-3.1.jar;$repo\org\apache\openejb\openejb-api\4.6.0\openejb-api-4.6.0.jar;$repo\org\apache\openejb\openejb-loader\4.6.0\openejb-loader-4.6.0.jar;$repo\org\apache\openejb\openejb-javaagent\4.6.0\openejb-javaagent-4.6.0.jar;$repo\org\apache\openejb\openejb-jee\4.6.0\openejb-jee-4.6.0.jar;$repo\com\sun\xml\bind\jaxb-impl\2.2.6\jaxb-impl-2.2.6.jar;$repo\org\apache\openejb\openejb-jee-accessors\4.6.0\openejb-jee-accessors-4.6.0.jar;$repo\org\metatype\sxc\sxc-jaxb-core\0.8\sxc-jaxb-core-0.8.jar;$repo\org\metatype\sxc\sxc-runtime\0.8\sxc-runtime-0.8.jar;$repo\org\apache\activemq\activemq-ra\5.9.0\activemq-ra-5.9.0.jar;$repo\org\apache\activemq\activemq-kahadb-store\5.9.0\activemq-kahadb-store-5.9.0.jar;$repo\org\apache\activemq\protobuf\activemq-protobuf\1.1\activemq-protobuf-1.1.jar;$repo\org\apache\activemq\activemq-broker\5.9.0\activemq-broker-5.9.0.jar;$repo\org\apache\activemq\activemq-client\5.9.0\activemq-client-5.9.0.jar;$repo\org\fusesource\hawtbuf\hawtbuf\1.9\hawtbuf-1.9.jar;$repo\org\apache\activemq\activemq-openwire-legacy\5.9.0\activemq-openwire-legacy-5.9.0.jar;$repo\org\apache\activemq\activemq-jdbc-store\5.9.0\activemq-jdbc-store-5.9.0.jar;$repo\org\apache\geronimo\components\geronimo-connector\3.1.1\geronimo-connector-3.1.1.jar;$repo\org\apache\geronimo\specs\geronimo-j2ee-connector_1.6_spec\1.0\geronimo-j2ee-connector_1.6_spec-1.0.jar;$repo\org\apache\geronimo\components\geronimo-transaction\3.1.1\geronimo-transaction-3.1.1.jar;$repo\org\objectweb\howl\howl\1.0.1-1\howl-1.0.1-1.jar;$repo\org\apache\geronimo\javamail\geronimo-javamail_1.4_mail\1.8.2\geronimo-javamail_1.4_mail-1.8.2.jar;$repo\org\apache\xbean\xbean-asm4-shaded\3.15\xbean-asm4-shaded-3.15.jar;$repo\org\apache\xbean\xbean-finder-shaded\3.15\xbean-finder-shaded-3.15.jar;$repo\org\apache\xbean\xbean-reflect\3.15\xbean-reflect-3.15.jar;$repo\org\apache\xbean\xbean-naming\3.15\xbean-naming-3.15.jar;$repo\org\apache\xbean\xbean-bundleutils\3.15\xbean-bundleutils-3.15.jar;$repo\org\hsqldb\hsqldb\2.3.0\hsqldb-2.3.0.jar;$repo\commons-dbcp\commons-dbcp\1.4\commons-dbcp-1.4.jar;$repo\commons-pool\commons-pool\1.5.7\commons-pool-1.5.7.jar;$repo\org\codehaus\swizzle\swizzle-stream\1.6.2\swizzle-stream-1.6.2.jar;$repo\commons-logging\commons-logging\1.1.1\commons-logging-1.1.1.jar;$repo\org\quartz-scheduler\quartz\2.2.0\quartz-2.2.0.jar;$repo\org\apache\openwebbeans\openwebbeans-impl\1.2.1\openwebbeans-impl-1.2.1.jar;$repo\org\apache\openwebbeans\openwebbeans-spi\1.2.1\openwebbeans-spi-1.2.1.jar;$repo\org\apache\openwebbeans\openwebbeans-ejb\1.2.1\openwebbeans-ejb-1.2.1.jar;$repo\org\apache\openwebbeans\openwebbeans-ee\1.2.1\openwebbeans-ee-1.2.1.jar;$repo\org\apache\openwebbeans\openwebbeans-ee-common\1.2.1\openwebbeans-ee-common-1.2.1.jar;$repo\org\apache\openwebbeans\openwebbeans-web\1.2.1\openwebbeans-web-1.2.1.jar;$repo\org\apache\openwebbeans\openwebbeans-el22\1.2.1\openwebbeans-el22-1.2.1.jar;$repo\org\apache\openejb\patch\openjpa\2.3.0-nonfinal-1540826\openjpa-2.3.0-nonfinal-1540826.jar;$repo\commons-lang\commons-lang\2.4\commons-lang-2.4.jar;$repo\commons-collections\commons-collections\3.2.1\commons-collections-3.2.1.jar;$repo\net\sourceforge\serp\serp\1.14.1\serp-1.14.1.jar;$repo\junit\junit\3.8.1\junit-3.8.1.jar;$repo\org\apache\bval\bval-core\0.5\bval-core-0.5.jar;$repo\commons-beanutils\commons-beanutils-core\1.8.3\commons-beanutils-core-1.8.3.jar;$repo\org\apache\bval\bval-jsr303\0.5\bval-jsr303-0.5.jar;$repo\org\fusesource\jansi\jansi\1.8\jansi-1.8.jar;$repo\log4j\log4j\1.2.15\log4j-1.2.15.jar;$repo\javax\mail\mail\1.4\mail-1.4.jar;$repo\javax\activation\activation\1.1\activation-1.1.jar;$repo\javax\jms\jms\1.1\jms-1.1.jar;$repo\com\sun\jdmk\jmxtools\1.2.1\jmxtools-1.2.1.jar;$repo\com\sun\jmx\jmxri\1.2.1\jmxri-1.2.1.jar;$repo\mysql\mysql-connector-java\5.1.30\mysql-connector-java-5.1.30.jar;$repo\org\slf4j\slf4j-log4j12\1.7.5\slf4j-log4j12-1.7.5.jar;$repo\org\slf4j\slf4j-api\1.7.5\slf4j-api-1.7.5.jar" \
io.cqrs.bench.runner.BencherTest -c 10000 -a 100000 -t 8 
