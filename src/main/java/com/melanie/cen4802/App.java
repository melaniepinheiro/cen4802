package com.melanie.cen4802;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A small, dependency-free Java web application for tracking restaurant tasks.
 */
public final class App {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, h:mm a");
    private static final TaskService TASKS = new TaskService();

    private App() {
    }

    public static void main(String[] args) throws IOException {
        int port = readPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", App::handleRequest);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("Restaurant Task Tracker is running.");
        System.out.println("Open http://localhost:" + port + " in your browser.");
    }

    private static int readPort() {
        String configuredPort = System.getenv().getOrDefault("PORT", "8080");
        try {
            return Integer.parseInt(configuredPort);
        } catch (NumberFormatException exception) {
            System.err.println("Invalid PORT value; using 8080.");
            return 8080;
        }
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equals(method) && "/".equals(path)) {
                showHomePage(exchange, null);
                return;
            }
            if ("GET".equals(method) && "/health".equals(path)) {
                sendResponse(exchange, 200, "application/json; charset=UTF-8", "{\"status\":\"UP\"}");
                return;
            }
            if ("POST".equals(method) && "/tasks".equals(path)) {
                addTask(exchange);
                return;
            }
            if ("POST".equals(method) && path.matches("/tasks/\\d+/toggle")) {
                changeTaskStatus(exchange, path);
                return;
            }
            if ("POST".equals(method) && path.matches("/tasks/\\d+/delete")) {
                deleteTask(exchange, path);
                return;
            }

            sendResponse(exchange, 404, "text/plain; charset=UTF-8", "Page not found");
        } catch (Exception exception) {
            exception.printStackTrace();
            sendResponse(exchange, 500, "text/plain; charset=UTF-8", "The server could not process the request.");
        }
    }

    private static void showHomePage(HttpExchange exchange, String errorMessage) throws IOException {
        List<Task> tasks = TASKS.findAll();
        long completedCount = tasks.stream().filter(Task::completed).count();
        String page = renderPage(tasks, completedCount, errorMessage);
        sendResponse(exchange, 200, "text/html; charset=UTF-8", page);
    }

    private static void addTask(HttpExchange exchange) throws IOException {
        Map<String, String> form = readForm(exchange);
        String description = form.getOrDefault("description", "").trim();
        String location = form.getOrDefault("location", "").trim();
        String priority = form.getOrDefault("priority", "Normal").trim();

        if (description.isBlank() || location.isBlank()) {
            showHomePage(exchange, "Enter both a task description and a store/location.");
            return;
        }

        TASKS.add(description, location, Priority.from(priority));
        redirect(exchange, "/");
    }

    private static void changeTaskStatus(HttpExchange exchange, String path) throws IOException {
        int id = Integer.parseInt(path.split("/")[2]);
        TASKS.toggle(id);
        redirect(exchange, "/");
    }

    private static void deleteTask(HttpExchange exchange, String path) throws IOException {
        int id = Integer.parseInt(path.split("/")[2]);
        TASKS.delete(id);
        redirect(exchange, "/");
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> values = new ConcurrentHashMap<>();
        if (body.isBlank()) {
            return values;
        }

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";
            values.put(key, value);
        }
        return values;
    }

    private static void redirect(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().set("Location", path);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    private static void sendResponse(HttpExchange exchange, int status, String contentType, String body)
            throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, response.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
        }
    }

    private static String renderPage(List<Task> tasks, long completedCount, String errorMessage) {
        StringBuilder taskCards = new StringBuilder();
        for (Task task : tasks) {
            taskCards.append(renderTask(task));
        }

        if (taskCards.isEmpty()) {
            taskCards.append("<div class=\"empty\">No tasks yet. Add the first task above.</div>");
        }

        String error = errorMessage == null
                ? ""
                : "<div class=\"error\">" + escapeHtml(errorMessage) + "</div>";

        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Restaurant Task Tracker</title>
                  <style>
                    :root { --navy:#17233c; --blue:#2e6be6; --green:#16845b; --red:#b42318; --paper:#fff; --bg:#f4f6fa; --line:#dfe4ec; }
                    * { box-sizing:border-box; }
                    body { margin:0; font-family:Arial,sans-serif; color:#172033; background:linear-gradient(135deg,#eef3ff,#f8fafc 48%%,#eef9f4); }
                    header { background:var(--navy); color:white; padding:28px 20px; }
                    header div, main { width:min(980px,calc(100%% - 32px)); margin:auto; }
                    h1 { margin:0 0 6px; font-size:clamp(1.7rem,4vw,2.5rem); }
                    header p { margin:0; color:#cbd5e1; }
                    main { padding:26px 0 48px; }
                    .panel { background:var(--paper); border:1px solid var(--line); border-radius:16px; padding:20px; box-shadow:0 12px 34px rgba(23,35,60,.08); margin-bottom:20px; }
                    .summary { display:grid; grid-template-columns:repeat(3,1fr); gap:12px; }
                    .stat { background:#f8fafc; border:1px solid var(--line); border-radius:12px; padding:16px; }
                    .stat strong { display:block; font-size:1.7rem; color:var(--navy); }
                    .stat span { color:#64748b; font-size:.9rem; }
                    h2 { margin:0 0 14px; font-size:1.2rem; }
                    form.add { display:grid; grid-template-columns:2fr 1.2fr .8fr auto; gap:10px; }
                    input, select, button { font:inherit; border-radius:9px; }
                    input, select { width:100%%; border:1px solid #cbd5e1; padding:11px 12px; background:white; }
                    button { border:0; padding:11px 15px; cursor:pointer; font-weight:700; }
                    .primary { color:white; background:var(--blue); }
                    .error { background:#fff1f0; border:1px solid #fecdca; color:var(--red); padding:10px 12px; border-radius:9px; margin-bottom:12px; }
                    .task { display:grid; grid-template-columns:1fr auto; gap:14px; padding:16px 0; border-bottom:1px solid var(--line); }
                    .task:last-child { border-bottom:0; padding-bottom:0; }
                    .task:first-child { padding-top:0; }
                    .task.done h3 { text-decoration:line-through; color:#64748b; }
                    .task h3 { margin:0 0 7px; }
                    .meta { display:flex; flex-wrap:wrap; gap:7px; color:#64748b; font-size:.86rem; }
                    .badge { border-radius:999px; padding:3px 8px; background:#eef2ff; color:#3743a5; }
                    .badge.high { background:#fff1f0; color:var(--red); }
                    .actions { display:flex; gap:7px; align-items:center; }
                    .toggle { color:#075e45; background:#e7f8f1; }
                    .delete { color:var(--red); background:#fff1f0; }
                    .empty { color:#64748b; text-align:center; padding:26px; }
                    footer { color:#64748b; text-align:center; font-size:.82rem; margin-top:24px; }
                    @media (max-width:760px) {
                      form.add { grid-template-columns:1fr; }
                      .summary { grid-template-columns:1fr; }
                      .task { grid-template-columns:1fr; }
                      .actions { justify-content:flex-start; }
                    }
                  </style>
                </head>
                <body>
                  <header><div><h1>Restaurant Task Tracker</h1><p>Keep daily store responsibilities organized in one place.</p></div></header>
                  <main>
                    <section class="panel summary">
                      <div class="stat"><strong>%d</strong><span>Total tasks</span></div>
                      <div class="stat"><strong>%d</strong><span>Completed</span></div>
                      <div class="stat"><strong>%d</strong><span>Still open</span></div>
                    </section>
                    <section class="panel">
                      <h2>Add a task</h2>
                      %s
                      <form class="add" method="post" action="/tasks">
                        <input name="description" maxlength="100" placeholder="Example: Check refrigerator temperature" required>
                        <input name="location" maxlength="50" placeholder="Store or location" required>
                        <select name="priority" aria-label="Priority">
                          <option>Normal</option>
                          <option>High</option>
                          <option>Low</option>
                        </select>
                        <button class="primary" type="submit">Add task</button>
                      </form>
                    </section>
                    <section class="panel"><h2>Current tasks</h2>%s</section>
                    <footer>Java %s · Health check available at <code>/health</code></footer>
                  </main>
                </body>
                </html>
                """.formatted(
                tasks.size(), completedCount, tasks.size() - completedCount,
                error, taskCards, Runtime.version().feature());
    }

    private static String renderTask(Task task) {
        String completedClass = task.completed() ? " done" : "";
        String buttonLabel = task.completed() ? "Reopen" : "Complete";
        String priorityClass = task.priority() == Priority.HIGH ? " high" : "";

        return """
                <article class="task%s">
                  <div>
                    <h3>%s</h3>
                    <div class="meta">
                      <span>%s</span>
                      <span class="badge%s">%s priority</span>
                      <span>Added %s</span>
                    </div>
                  </div>
                  <div class="actions">
                    <form method="post" action="/tasks/%d/toggle"><button class="toggle" type="submit">%s</button></form>
                    <form method="post" action="/tasks/%d/delete"><button class="delete" type="submit">Delete</button></form>
                  </div>
                </article>
                """.formatted(
                completedClass,
                escapeHtml(task.description()),
                escapeHtml(task.location()),
                priorityClass,
                escapeHtml(task.priority().displayName()),
                task.createdAt().format(TIME_FORMAT),
                task.id(),
                buttonLabel,
                task.id());
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private enum Priority {
        LOW("Low"), NORMAL("Normal"), HIGH("High");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        String displayName() {
            return displayName;
        }

        static Priority from(String value) {
            for (Priority priority : values()) {
                if (priority.displayName.equalsIgnoreCase(value)) {
                    return priority;
                }
            }
            return NORMAL;
        }
    }

    private record Task(
            int id,
            String description,
            String location,
            Priority priority,
            boolean completed,
            LocalDateTime createdAt) {

        Task toggle() {
            return new Task(id, description, location, priority, !completed, createdAt);
        }
    }

    private static final class TaskService {
        private final AtomicInteger nextId = new AtomicInteger(1);
        private final CopyOnWriteArrayList<Task> tasks = new CopyOnWriteArrayList<>();

        TaskService() {
            add("Check refrigerator temperature", "Tallahassee", Priority.HIGH);
            add("Review opening checklist", "Boca Raton", Priority.NORMAL);
            add("Confirm sanitizer concentration", "Lutz", Priority.NORMAL);
        }

        List<Task> findAll() {
            List<Task> copy = new ArrayList<>(tasks);
            copy.sort(Comparator.comparing(Task::createdAt).reversed());
            return copy;
        }

        void add(String description, String location, Priority priority) {
            tasks.add(new Task(
                    nextId.getAndIncrement(),
                    description,
                    location,
                    priority,
                    false,
                    LocalDateTime.now()));
        }

        void toggle(int id) {
            tasks.replaceAll(task -> task.id() == id ? task.toggle() : task);
        }

        void delete(int id) {
            tasks.removeIf(task -> task.id() == id);
        }
    }
}
