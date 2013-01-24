Backend Java API for interaction with CloudMine
======

If this project is a subproject of the Android library, run package.sh. It will build the entire library for you, putting the .jar in android_root/cloudmine-javasdk/target.


Build from scratch instructions:

1. In these instructions, the version number may need to be changed depending on the actual version of the project you are working on.
1. Edit the pom.xml file so that the version number is correct.
1. Building the Android Library is a involved processes, because it relies on the Java Library, which we keep as a seperate project. We first need to build the Java Library, and then we can go ahead and build the Android Library:  
<code>mvn package -DskipTests=true -f $JAVA_SDK_HOME/pom.xml;mvn install:install-file -DgroupId=com.cloudmine.api -DartifactId=cloudmine-javasdk -Dversion=0.3-SNAPSHOT -Dpackaging=jar -DpomFileJAVA_SDK_HOME/pom.xml -DfileJAVA_SDK_HOME/target/cloudmine-javasdk-0.3.jar -DgeneratePom=true</code>
1. Once the Java Library has been built, we can build the Android Library. <code>$ANDROID_HOME</code> is the top directory of the Android project. 
<code>mvn package -DskipTests=true -f $ANDROID_HOME/pom.xml;mvn install:install-file -DgroupId=com.cloudmine.api -DartifactId=cloudmine-android -Dversion=0.3-SNAPSHOT -Dpackaging=jar -DpomFile=$ANDROID_HOME/pom.xml -Dfile=$ANDROID_HOME/target/cloudmine-android-0.3-SNAPSHOT.jar</code>
1. Run all the integration test and unit tests. Fix any that are failing.
1. Build the jar.  
6.1 For Android: <code>mvn assembly:assembly -DdescriptorId=jar-with-dependencies -DbuildFor=android -DskipTests=true</code>  
6.2 For Java: <code>mvn package -DskipTests=true</code>
1. Rename the .jar from cloudmine-javasdk-v#.#-jar-with-dependencies.jar to cloudmine-android-v#.#.jar
1. Test that the jar works by following the set up instructions in the Android documentations, using the newly built jar instead of the downloadable jar, and then running the project.
1. Generate JavaDoc jar: <code>mvn javadoc:jar</code>
1. Rename the jar to cloudmine-android-v0.#-javadoc.jar
1. Zip up the javadoc jar and the main jar, make sure the zip file is named "cloudmine-android-v0.#.zip"
1. Upload zip file.