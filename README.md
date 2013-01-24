Backend Java API for interaction with CloudMine
======

Build from scratch instructions:  
1. If there have been any changes to the android project itself OR this is your first time doing a build from scratch, you must build the android project first. The android project depends on the JavaSDK, so you must build and add that to your local maven repository first:  
mvn package -DskipTests=true -f $JAVA_SDK_HOME/pom.xml;mvn install:install-file -DgroupId=com.cloudmine.api -DartifactId=cloudmine-javasdk -Dversion=0.3-SNAPSHOT -Dpackaging=jar -DpomFileJAVA_SDK_HOME/pom.xml -DfileJAVA_SDK_HOME/target/cloudmine-javasdk-0.3.jar -DgeneratePom=true  
Then use this command to build and add the android sdk:
mvn package -DskipTests=true -f $ANDROID_HOME/pom.xml;mvn install:install-file -DgroupId=com.cloudmine.api -DartifactId=cloudmine-android -Dversion=0.3-SNAPSHOT -Dpackaging=jar -DpomFile=$ANDROID_HOME/pom.xml -Dfile=$ANDROID_HOME/target/cloudmine-android-0.3-SNAPSHOT.jar  
where $ANDROID_HOME = the home directory of the android project.  
If you haven't  
2. Run all of the integration and unit tests.  
3. Edit the pom.xml so that the version # is correct  
3. Build the jar  
3.1 for android, the command is: mvn assembly:assembly -DdescriptorId=jar-with-dependencies -DbuildFor=android -DskipTests=true  
3.2 for javasdk, the command is: mvn package -DskipTests=true  
4. Rename the jar from cloudmine-javasdk-0.#-jar-with-dependencies.jar to cloudmine-android-v0.#.jar  
5. Test that the jar works by following the set up instructions in the Android documentations, using the newly built jar instead of the downloadable jar, and then running the project.  
6. Generate JavaDoc jar: mvn javadoc:jar   
7. Rename the jar to cloudmine-android-v0.#-javadoc.jar  
8. Zip up the javadoc jar and the main jar, make sure the zip file is named "cloudmine-android-v0.#.zip"  
9. Go to https://github.com/cloudmine/cloudmine-android/downloads select "Choose a new file", select the zip file you created, and upload it  
10. Update the links on the website  
11. Deploy locally, redownload the zip, and test that the jar works  
12. Redeploy the CloudMine website  
13. Tag the build: git tag -a v0.# -m 'version 0.#', then push to the main repo: git push --tags  