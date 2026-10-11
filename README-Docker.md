# Restaurant Task Tracker: Docker Assignment Guide

Melanie Pinheiro | CEN4802C

This guide continues the existing project at https://github.com/melaniepinheiro/cen4802. It was prepared against commit `cc5aaa45f98aa66a7c744b8d9828753cf596f379`.

**This is a setup guide, not a completed screenshot report.** Run the commands on your computer and collect real screenshots. The caption examples below should be used only after your results match them. Record any errors and the steps that actually fixed them.

## Start here

Copy `Dockerfile`, `.dockerignore`, and this guide into your existing IntelliJ project folder, beside `pom.xml`. Keep your existing Java code, tests, build settings, and GitHub Actions workflow.

Use the **PowerShell terminal** in IntelliJ. Run commands from the folder that contains `pom.xml`. Run each command separately, and check its result before continuing. If a build fails, fix it before building a Docker image.

Your existing application uses Java 17, Maven, and the JDK HTTP server. It listens on port 8080 by default. The Dockerfile uses the same Java major version and starts the JAR with the same HTTP-server module as your earlier startup command.

The workflow is:

**Java source -> Maven compile/test/package -> JAR -> Docker image -> running container -> browser**

Maven remains responsible for compilation, unit tests, and packaging. Docker receives the already-built JAR. The Dockerfile does not run Maven or rebuild the Java source.

## 1. Install and check Docker

Install Docker Desktop for Windows from the official page:
https://docs.docker.com/desktop/setup/install/windows-install/

Follow the installer instructions for your computer. Use the WSL 2 backend when available and Linux containers. Complete any required restart, then open Docker Desktop and wait for the engine to start. If Docker was newly installed, reopen IntelliJ so its terminal picks up the updated PATH.

Run:

```powershell
docker --version
docker info
docker run --rm hello-world
```

**Screenshot 1:** Show the commands and successful output. You may use two images if the output is too long to read.

**Explain:** `docker --version` identifies the installed command-line client. `docker info` must also return server information; a client version alone does not prove the engine is working. The hello-world result shows that Docker can download an image and start a container. The `--rm` option removes that test container when it exits.

## 2. Show the repository and project files

In IntelliJ's Project panel, expand:

- `src/main/java/com/melanie/cen4802/App.java`
- `src/test/java/com/melanie/cen4802/AppTest.java`
- `pom.xml`
- `.github/workflows/maven.yml`
- `Dockerfile`
- `.dockerignore`
- `README.md` and `README-Docker.md`

Run:

```powershell
git remote -v
git status --short
```

**Screenshot 2:** Show the expanded project and the terminal output. If needed, use a separate screenshot of the repository page.

**Explain:** The app and tests remain in the existing repository. `pom.xml` defines Java 17, the JUnit test dependency, and the plugins used by Maven. The GitHub Actions workflow already uses Maven to build, test, and package this project. The two new Docker files control container packaging.

## 3. Build, test, and package the original application

Run:

```powershell
mvn clean package
Get-Item .\target\restaurant-task-tracker-1.0.0.jar | Select-Object Name, Length, LastWriteTime
Get-FileHash .\target\restaurant-task-tracker-1.0.0.jar -Algorithm SHA256 | Format-List
```

**Screenshot 3A:** Capture the Maven command, test summary, JAR creation line, and `BUILD SUCCESS`. The current project has four tests. Use your actual result in the report.

**Screenshot 3B:** Capture the JAR filename and its SHA-256 hash. Keep this image for comparison with the second build.

**Explain:** `clean` removes the previous Maven build output. `package` compiles the application and tests, runs the tests, and creates the JAR in `target`. The JAR is the packaged Java program. The Docker image will contain this JAR plus the Java runtime needed to execute it. A file hash is a fingerprint of the file's bytes.

## 4. Show the Dockerfile

The supplied Dockerfile contains:

```dockerfile
# Build and test the application first with: mvn clean package
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the packaged artifact from the existing Maven build.
COPY target/restaurant-task-tracker-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "--add-modules", "jdk.httpserver", "-jar", "app.jar"]
```

**Screenshot 4:** Show the complete Dockerfile in the editor.

**Explain each part:**

| Instruction | Purpose in this project |
| --- | --- |
| `FROM eclipse-temurin:17-jre` | Supplies the Java 17 runtime and its base operating-system environment. |
| `WORKDIR /app` | Sets the directory where the application runs. |
| `COPY ... app.jar` | Copies the JAR produced by Maven into the image and names it `app.jar`. |
| `EXPOSE 8080` | Documents the port used inside the container. Publishing a port still requires `docker run -p`. |
| `ENTRYPOINT [...]` | Starts the packaged Java application when the container starts. It enables the JDK HTTP-server module used by this app. |

The `.dockerignore` file sends only the Docker configuration and the required JAR to the image build. It keeps source folders, Git history, IDE files, and unrelated build output out of the build context. The JAR must remain available to Docker, even though generated JARs should not be committed to Git or included in the final source submission.

## 5. Build the first image

Run only after Maven reports success:

```powershell
docker build --progress=plain -t restaurant-task-tracker:v1 .
```

**Screenshot 5:** Show the command and successful end of the build output. Depending on the Docker version, this may include image export, naming, and completed steps.

**Explain:** Docker uses the current directory as its build context; that is what the final dot means. It reads the Dockerfile and packages the Maven-produced JAR into the image. The `v1` tag identifies the first version for this demonstration.

## 6. Show the built image

```powershell
docker images restaurant-task-tracker
docker image inspect restaurant-task-tracker:v1 --format '{{.Id}}'
```

**Screenshot 6:** Capture the repository name, `v1` tag, image ID, creation information, and image size. Keep the full image ID for the later comparison.

**Explain:** An image is the packaged template used to create containers. It is different from a running container. The tag is a readable name; the image ID identifies the image's content and configuration.

## 7. Run the first container

Stop any app started using IntelliJ's Run button or your earlier Java command so the demonstration is clear. Then run:

```powershell
docker run -d --name restaurant-v1 -p 127.0.0.1:8081:8080 restaurant-task-tracker:v1
docker ps --filter name=restaurant-v1
docker logs restaurant-v1
docker port restaurant-v1
```

**Screenshot 7:** Show the run command, the running container, the port mapping, and the application startup log. Use two images if needed.

**Explain:** `-d` runs the container in the background. The name is `restaurant-v1`. Requests to port **8081 on your computer** are sent to port **8080 inside the container**. The `127.0.0.1` binding makes this demonstration available through your own computer. The app may print `localhost:8080` in its log because that is its internal port; your browser should use **8081**.

## 8. Check the application in the browser

Open http://localhost:8081.

1. Add a task such as `Check refrigerator temperature` at `Lutz` with `High` priority.
2. Confirm that the new task appears and the counts change.
3. Mark the task complete and check the Completed filter.
4. Capture the page before deleting the demonstration task.

Also run:

```powershell
curl.exe -i http://localhost:8081/health
```

**Screenshot 8A:** Show the browser address bar and the added task.

**Screenshot 8B:** Show the completed task or filtered result, plus the health request output in a separate image if needed. The expected health response is HTTP 200 with `{"status":"UP"}`.

**Explain:** Adding and completing a task checks actual app behavior. A health response alone only checks that the server responds. The browser uses port 8081, which matches the Docker port mapping shown in Screenshot 7.

For stronger evidence that Docker is serving the page, run the following after saving your task screenshots:

```powershell
docker stop restaurant-v1
curl.exe --max-time 5 -i http://localhost:8081/health
docker start restaurant-v1
curl.exe -i http://localhost:8081/health
```

The request should fail while the container is stopped and succeed again after it starts. Wait briefly for startup if the last request is too early. Capture this as **Screenshot 8C**. This app stores tasks in memory, so stopping it resets the task list; persistent storage is not part of the existing application.

## 9. Change the source, rebuild, and prove the update

Keep the original `v1` image and container. You will run `v2` beside it on another host port.

### A. Make a visible source-code change

Open `src/main/java/com/melanie/cen4802/App.java`.

Change:

```html
<title>Restaurant Task Tracker - CI Updated</title>
```

to:

```html
<title>Restaurant Task Tracker - Docker Updated</title>
```

Also change this existing header line:

```html
<header><div><h1>Restaurant Task Tracker</h1><p>Updated with Maven for Assignment 2.</p></div></header>
```

to:

```html
<header><div><h1>Restaurant Task Tracker - Docker Updated</h1><p>Updated version running in Docker for Assignment 5.</p></div></header>
```

Save the file, then run:

```powershell
git diff -- src/main/java/com/melanie/cen4802/App.java
```

**Screenshot 9A:** Capture the code change or Git diff. Changing the heading and subtitle makes the update visible in the page itself, not just the browser tab.

### B. Repeat the existing Maven build

```powershell
mvn clean package
Get-Item .\target\restaurant-task-tracker-1.0.0.jar | Select-Object Name, Length, LastWriteTime
Get-FileHash .\target\restaurant-task-tracker-1.0.0.jar -Algorithm SHA256 | Format-List
```

**Screenshot 9B:** Show the new successful build and test results. Capture the new JAR details and hash as **Screenshot 9C** if they do not fit clearly.

The JAR filename stays the same because the Maven project version remains 1.0.0. Its contents now contain the source change. Compare the new hash with Screenshot 3B. A changed hash alone does not prove the specific page change, so also collect the runtime evidence below.

### C. Build and run the updated image

```powershell
docker build --progress=plain -t restaurant-task-tracker:v2 .
docker images restaurant-task-tracker
docker image inspect restaurant-task-tracker:v2 --format '{{.Id}}'
```

**Screenshot 9D:** Show the successful rebuild and both image versions. Use an additional image if the full output is too long. Compare the `v2` image ID with the original `v1` image ID.

```powershell
docker run -d --name restaurant-v2 -p 127.0.0.1:8082:8080 restaurant-task-tracker:v2
docker ps --filter name=restaurant-v
docker logs restaurant-v2
docker inspect --format '{{.Name}} | tag={{.Config.Image}} | image={{.Image}}' restaurant-v1 restaurant-v2
```

**Screenshot 9E:** Show both containers and the updated app startup. The inspect output connects each running container to its image ID; the `restaurant-v2` ID should match the `v2` image inspected above.

### D. Match the packaged JAR to the running container

```powershell
docker exec restaurant-v1 sha256sum /app/app.jar
docker exec restaurant-v2 sha256sum /app/app.jar
```

**Screenshot 9F:** Show both hashes. The hash inside `restaurant-v2` should match the local JAR from Screenshot 9C; compare the hexadecimal characters without regard to letter case. The `restaurant-v1` hash should match the original JAR from Screenshot 3B.

### E. Show the updated application

Open these pages in two browser windows:

- Original container: http://localhost:8081
- Updated container: http://localhost:8082

Refresh both pages. The second page should show **Restaurant Task Tracker - Docker Updated** and the new Assignment 5 message. Add and complete a task in the updated page to confirm its behavior still works.

```powershell
curl.exe -i http://localhost:8082/health
```

**Screenshot 9G:** Show the original and updated pages with both address bars visible. Add another screenshot for the updated app's task behavior or HTTP check if needed.

**Discussion example, after checking your actual evidence:**

> I changed the heading and message in App.java, then ran the same Maven build again. The build ran the tests and created a new JAR. The updated JAR had a different hash from the first version. I built a second Docker image with the v2 tag. Its image ID matched the image used by the restaurant-v2 container. The JAR hash inside that container also matched the new Maven JAR. Finally, the page on port 8082 showed the new heading, while the original container on port 8081 still showed the old page. These checks connect the source change to the packaged file, image, and running app.

## Update the README and keep your work in Git

The repository's current README says there are no automated tests. Replace that outdated sentence with:

> The project uses four JUnit tests in src/test/java/com/melanie/cen4802/AppTest.java. Running mvn clean package runs the tests and creates the application JAR. Tasks are stored in memory and reset when the application stops. See README-Docker.md for the Docker build, run, and verification steps.

Add an acknowledgement such as:

> This project continues my work from Assignments 1 through 4. ChatGPT assisted with the starter application, Docker configuration, and instructions. I used Docker's official documentation for the container workflow.

Review the files, then commit the work:

```powershell
git status --short
git add Dockerfile .dockerignore README-Docker.md README.md src/main/java/com/melanie/cen4802/App.java
git commit -m "Add Docker configuration and updated application heading"
git push origin main
```

If a push is rejected because the remote branch has newer work, do not force-push. Read the Git message and resolve the difference before continuing.

## Prepare the source ZIP and screenshot report

After committing your final files, you can export only the tracked project files:

```powershell
git archive --format=zip --output=..\Melanie_Pinheiro_Docker_Source.zip HEAD
```

Open the ZIP and confirm it includes the Java source, unit tests, `pom.xml`, Docker files, README files, supporting scripts, and existing CI configuration. The archive must contain your final committed update. Do not include `target`, `out`, JARs, images exported from Docker, `.git`, dependency caches, or IDE temporary files.

Create the Word/PDF report using the same nine sections as this guide. Put each readable screenshot next to its caption and explanation. Use extra figures instead of shrinking long output until it cannot be read. Include actual image IDs, relevant hashes, results, and troubleshooting observations from your run. Do not submit this instruction guide as the completed evidence report.

Suggested report introduction:

> For this assignment, I continued using my Restaurant Task Tracker Java web application. Maven builds the project, runs the JUnit tests, and packages the app into a JAR. Docker then packages that JAR with Java 17 so the application can run in a container. I checked the app in a browser, changed the page heading, and repeated the build and container steps to verify the updated version.

Use that introduction only after completing the described work. If a step remains incomplete, explain what you tried and the exact error instead.

When all screenshots are saved, stop the two demonstration containers if you are finished using them:

```powershell
docker stop restaurant-v1 restaurant-v2
```

## Troubleshooting

| Problem | What to check |
| --- | --- |
| Docker command is not found | Finish installing Docker Desktop and reopen the terminal or IntelliJ. |
| Cannot connect to the Docker engine | Open Docker Desktop, wait for startup, and check `docker info` again. Follow any displayed WSL or virtualization instructions. |
| Maven reports no POM | Run the command from the folder that contains `pom.xml`. |
| Maven tests or compilation fail | Fix the reported problem and run `mvn clean package` again. Do not skip tests or continue with an old JAR. |
| Docker cannot find the JAR during COPY | Confirm the Maven build succeeded and created the exact filename in the Dockerfile. Check `.dockerignore` allows that JAR. |
| Port 8081 or 8082 is already allocated | Identify the app or container using that port, or choose a free host port and use that same port in the browser. The container port remains 8080. |
| Container name is already in use | Check `docker ps -a`. Use a new name, or remove only an earlier disposable demonstration container after saving its evidence. Removing a container does not remove its image. |
| Container exits immediately | Run `docker ps -a` and `docker logs restaurant-v1` or `docker logs restaurant-v2` to read the actual startup error. |
| Browser cannot connect | Check the container is Up, use the correct host port, and confirm the mapping with `docker port`. This project's server already binds to all interfaces. |
| Old page still appears | Save the source, rerun Maven, rebuild the image, start a new container from the new tag, and open the correct URL. Restarting the old container does not update its image. Compare the JAR hashes. |

## Sources

- Existing project: https://github.com/melaniepinheiro/cen4802
- Docker Desktop for Windows: https://docs.docker.com/desktop/setup/install/windows-install/
- Dockerfile instructions: https://docs.docker.com/reference/dockerfile/
- Port publishing: https://docs.docker.com/get-started/docker-concepts/running-containers/publishing-ports/
- Eclipse Temurin official image: https://hub.docker.com/_/eclipse-temurin
- Docker image building: https://docs.docker.com/get-started/tutorials/run-an-app/
- ChatGPT assistance with Docker setup and documentation.
