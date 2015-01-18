del /Q .\distr\*

mkdir .\distr\war

copy .\usci\modules\core\target\core-0.0.1-SNAPSHOT.one-jar.jar .\distr
copy .\usci\modules\sync\target\sync-0.0.1-SNAPSHOT.one-jar.jar .\distr
copy .\usci\modules\receiver\target\receiver.one-jar.jar .\distr
copy .\usci\modules\brms\rulesvr\target\rulesvr-0.0.1-SNAPSHOT.one-jar.jar .\distr
copy .\usci\modules\cli\target\cli-0.0.1-SNAPSHOT.one-jar.jar .\distr

copy .\usci\modules\portlets\baseentity_portlet\target\baseentity_portlet-0.0.1-SNAPSHOT.war .\distr\war
copy .\usci\modules\portlets\batch_portlet\target\batch_portlet-0.0.1-SNAPSHOT.war .\distr\war
copy .\usci\modules\portlets\credit-registry-administration-portlet\target\credit-registry-administration-portlet.war .\distr\war
copy .\usci\modules\portlets\credit-registry-protocol-portlet\target\credit-registry-protocol-portlet.war .\distr\war
copy .\usci\modules\portlets\credit-registry-report-portlet\target\credit-registry-report-portlet.war .\distr\war
copy .\usci\modules\portlets\credit-registry-upload-portlet\target\credit-registry-upload-portlet.war .\distr\war
copy .\usci\modules\portlets\drools_portlet\target\drools_portlet-0.0.1-SNAPSHOT.war .\distr\war
