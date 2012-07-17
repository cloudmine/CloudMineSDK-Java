THIS_VERSION = "0.0.1-BETA"

repositories.remote << 'http://repo.maven.apache.org/maven2/'
repositories.remote << 'https://oss.sonatype.org/content/repositories/snapshots/'


ANDROID = 'com.google.android:android:jar:2.3.3'
HTTP_NIO = 'org.apache.httpcomponents:httpcore-nio:jar:4.2'
HTTP_ASYNC = 'org.apache.httpcomponents:httpasyncclient:jar:4.0-beta1'
JACKSON_CORE = 'com.fasterxml.jackson.core:jackson-core:jar:2.0.0'
JACKSON_ANNOTATIONS = 'com.fasterxml.jackson.core:jackson-annotations:jar:2.0.2'
ROBOLECTRIC = 'com.pivotallabs:robolectric:jar:1.2-SNAPSHOT'

HTTP_CORE = 'org.apache.httpcomponents:httpcore:jar:4.0-beta3' 
HTTP_CLIENT = 'org.apache.httpcomponents:httpclient:jar:4.0-beta2'
JACKSON = 'com.fasterxml.jackson.core:jackson-databind:jar:2.0.0-RC2'
LOGGING_BASE = 'org.slf4j:slf4j-api:jar:1.6.4'
LOGGING_TYPE = 'org.slf4j:slf4j-simple:jar:1.6.4'
JODA = 'joda-time:joda-time:jar:2.1'
ANNOVENTION = 'tv.cntt:annovention:jar:1.0'
COMMONS = 'commons-io:commons-io:jar:2.3'
define 'cloudmine-javasdk' do
  project.version = '0.1'
  compile.with HTTP_CORE, HTTP_CLIENT, JACKSON, JACKSON_CORE, JACKSON_ANNOTATIONS, LOGGING_BASE, LOGGING_TYPE, COMMONS, JODA, ANNOVENTION
  package :jar
end
