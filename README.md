SharingPrediction
================

Toolkit for making evaluating predictions and recommendations about sharing

# Quick Start with Eclipse

The following is a guide to getting up an running as quickly as possible. The only assumption this quick start guide
makes is that you have Eclipse installed.

## Step 1: Get Maven

Download Maven from http://maven.apache.org, extract it, and add the binaries folder to your system path.
(Details:
IN WINDOWS OS - Add the binaries folder to system path as follows:
-Go to Control Panel
-In the search bar, type in 'Environment'
-Click on 'Edit environment variables for your account
-Double-click on the PATH variable and in the variable value field, add the binaries folder path to the
after the semicolon.)

Make sure the `JAVA_HOME` environment variable is set.
(Details:
IN WINDOWS OS - Set the JAVA_HOME environment variable as follows:
-Go to Control Panel
-In the search bar, type in 'Environment'
-Click on 'Edit the system environment variables'
-Under the 'Advanced' tab, click the 'Environment Variables' button
-In the System variables table, edit the JAVA_HOME variable and set the variable value to the folder
path of the Java Development Kit -- at the time of this writing, that is jre7.)

## Step 2: Setup Eclipse with Maven

From the **Help** menu select **Eclipse Marketplace**.

Install the plugin called *Maven Integration for Eclipse*. This will require Eclipse to restart.

After restarting, go to **Window->Preferences** in Eclipse, and then go to **Maven->Installations**.  
Add the install of Maven from Step 1.

## Step 3: Get and Initialize the Repository

Clone the repository.

```
git clone https://github.com/jwbartel/SharingPredictionEvaluation.git
```

From a command line (if using Windows, use command prompt not powershell), navigate to the folder you just cloned and
run the following command.

```
mvn install:install-file -Dfile=oeall17.jar -DgroupId=edu.unc -DartifactId=oeall -Dversion=17 -Dpackaging=jar
```

## Step 4: Add the Project to Eclipse

In Eclipse, import a Maven project. Select the project you just cloned.

*Note:* The compliance level may be set to **1.5** so be sure to change this to **1.7**.
Do this by right-clicking on the project and go to Build Path.
Click on Configure Build Path.
Click on Java Compiler, and uncheck the option "Use compliance from execution environment 'J2SE-1.5' 
on the 'Java Build Path'".
Change the version from 1.5 to 1.7 in the drop-down menu and click ok.

## Step 5: Add Dependencies to the Project

Right-click on the project and click on Build Path.
Click on Configure Build Path.
Go to the 'Projects' tab and click on 'Add'.
Select the projects you want to add and click 'Ok'.
In order to use SharingPredictionEvaluation and SOMINT, both of them must have the other project added
as a dependency in order to resolve errors that may be appearing after importing.

## Step 6: Use the Toolkit

That's it, you're all set up. You may now use the toolkit to evaluate predictions
