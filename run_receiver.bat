set JAVA_BIN_PATH="c:\Progra~1\Java\jdk1.7.0\bin\"

%JAVA_BIN_PATH%java -Djava.security.policy=./distr/no.policy -Dspring.profiles.active=oracle -jar ./distr/receiver.one-jar.jar

pause