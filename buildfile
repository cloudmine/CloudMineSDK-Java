repositories.remote << 'http://repo.maven.apache.org/maven2/'

HTTP_CORE = 'org.apache.httpcomponents:httpcore:jar:4.2' 
HTTP_CLIENT = 'org.apache.httpcomponents:httpclient:jar:4.2'
HTTP_NIO = 'org.apache.httpcomponents:httpcore-nio:jar:4.2'
HTTP_ASYNC = 'org.apache.httpcomponents:httpasyncclient:jar:4.0-beta1'
JACKSON = 'com.fasterxml.jackson.core:jackson-databind:jar:2.0.2'
JACKSON_CORE = 'com.fasterxml.jackson.core:jackson-core:jar:2.0.0'
JACKSON_ANNOTATIONS = 'com.fasterxml.jackson.core:jackson-annotations:jar:2.0.2'
LOGGING_BASE = 'org.slf4j:slf4j-api:jar:1.6.4'
LOGGING_TYPE = 'org.slf4j:slf4j-simple:jar:1.6.4'
JODA = 'joda-time:joda-time:jar:2.1'
COMMONS = 'org.apache.commons:commons-io:jar:1.3.2'
define 'cloudmine-javasdk' do
  project.version = '0.1'
  compile.with HTTP_CORE, HTTP_CLIENT, HTTP_NIO, HTTP_ASYNC, JACKSON, JACKSON_CORE, JACKSON_ANNOTATIONS, LOGGING_BASE, LOGGING_TYPE, COMMONS, JODA
  package :jar
end
