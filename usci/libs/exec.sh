mvn install:install-file -Dfile=couchbase-client-1.1.4.jar -DgroupId=couchbase -DartifactId=couchbase-client -Dversion=1.1.4 -Dpackaging=jar

mvn install:install-file -Dfile=spymemcached-2.8.12.jar -DgroupId=spy -DartifactId=spymemcached -Dversion=2.8.12 -Dpackaging=jar

mvn install:install-file -Dfile=easyuploads-0.5.1.jar -DgroupId=org.vaadin.addons -DartifactId=easyuploads -Dversion=0.5.1 -Dpackaging=jar

mvn install:install-file -Dfile=ojdbc6.jar -DgroupId=com.oracle -DartifactId=ojdbc6 -Dversion=11.2.0.3.0 -Dpackaging=jar