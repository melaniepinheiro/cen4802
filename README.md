# Restaurant Task Tracker

**Author:** Melanie Pinheiro

Restaurant Task Tracker is a small Java web application for organizing daily tasks across restaurant locations. Users can add a task, choose its priority, mark it complete or reopen it, and delete it. The page also calculates total, completed, and open task counts.

The project uses only Java's standard library, including the built-in `HttpServer`, so it has no external runtime dependencies. It provides a manageable foundation for later DevOps work such as automated testing, continuous integration, containerization, deployment, monitoring, and security analysis.

## Requirements

- JDK 17 or newer
- A web browser

> Make sure a **JDK** is installed, not only a Java Runtime Environment (JRE).

## Run on Windows

Double-click `run.bat`, or open PowerShell/Command Prompt in the project folder and run:

```text
run.bat
```

## Run on macOS or Linux

```bash
chmod +x run.sh
./run.sh
```

Then open [http://localhost:8080](http://localhost:8080) in a browser. Stop the server with `Ctrl+C` in the terminal.

## Optional Maven build

If Maven is installed, the project can also be compiled with:

```bash
mvn clean package
java --add-modules jdk.httpserver -jar target/restaurant-task-tracker-1.0.0.jar
```

## Application endpoints

- `GET /` - displays the task tracker
- `POST /tasks` - adds a task
- `POST /tasks/{id}/toggle` - completes or reopens a task
- `POST /tasks/{id}/delete` - deletes a task
- `GET /health` - returns a simple JSON health response

## Data storage

Tasks are stored in memory for this first version. They reset when the application stops. Persistent storage can be added during a later assignment.

## Acknowledgement

This project was created by Melanie Pinheiro with guidance and code assistance from OpenAI ChatGPT. The web server uses the `jdk.httpserver` package included with the Java Development Kit.
