Restaurant Task Tracker

Author: Melanie Pinheiro
Course: CEN 4802

About the App

This is my Java web app from Assignment 1. It helps users keep track of tasks at restaurant locations. For Assignment 2, I used Maven to build the app and make a JAR file.

Features

Add a task and store location.

Choose a priority.

Complete, reopen, or delete tasks.

View total, completed, and open task counts.

Filter the list by All tasks, Open, or Completed.

What You Need

JDK 17 or newer

Maven 3

A web browser

Internet access for the first build downloads

I used Java 17.0.18 and Maven 3.9.15 on Windows.

Build and Run

Open PowerShell in the project folder and run:

mvn clean package

Maven reads pom.xml, compiles the code, and creates this file:

target/restaurant-task-tracker-1.0.0.jar

Run the JAR:

java --add-modules jdk.httpserver -jar .\target\restaurant-task-tracker-1.0.0.jar

Open http://localhost:8080 in a browser. Keep PowerShell open while using the app. Press Ctrl+C in PowerShell to stop it.

Assignment 2 Change

I changed the message below the page heading to Updated with Maven for Assignment 2.

To see a code change, stop the app, save the file, and run the build and JAR commands again. Then refresh the browser.

Main Files

src/main/java/com/melanie/cen4802/App.java: the app's Java code.

pom.xml: the Maven build settings.

run.bat and run.sh: the earlier run scripts from Assignment 1.

.gitignore: keeps generated files out of Git.

Notes

Tasks reset when the app stops. There are no automated tests yet. I checked the app in the browser and saw the new message after rebuilding.

Leave the target and out folders, JAR files, and dependency caches out of the submission. Maven makes the build files again when needed.
