export MAVEN_OPTS="-XX:+CMSClassUnloadingEnabled -XX:PermSize=512M -XX:MaxPermSize=512M"

mvn jetty:run 
