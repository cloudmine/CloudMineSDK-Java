repositories.remote << 'http://repo.maven.apache.org/maven2/'

HTTP_CORE = 'org.apache.httpcomponents:httpcore:jar:4.2' 
HTTP_CLIENT = 'org.apache.httpcomponents:httpclient:jar:4.1.3'
JACKSON = 'com.fasterxml.jackson.core:jackson-databind:jar:2.0.0'
JACKSON_CORE = 'com.fasterxml.jackson.core:jackson-core:jar:2.0.0'
define 'cloudmine-javasdk' do
  project.version = '0.1'
  compile.with HTTP_CORE, HTTP_CLIENT, JACKSON, JACKSON_CORE
  package :jar
end
