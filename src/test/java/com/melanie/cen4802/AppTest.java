package com.melanie.cen4802;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void priorityParsingWorks() {
        assertEquals(App.Priority.HIGH, App.Priority.from("High"));
        assertEquals(App.Priority.LOW, App.Priority.from("low"));
        assertEquals(App.Priority.NORMAL, App.Priority.from("unknown"));
    }

    @Test
    void taskToggleChangesCompletionStatus() {
        App.Task task = new App.Task(
                1,
                "Check refrigerator",
                "Lutz",
                App.Priority.HIGH,
                false,
                LocalDateTime.now()
        );

        App.Task completedTask = task.toggle();

        assertFalse(task.completed());
        assertTrue(completedTask.completed());
        assertFalse(completedTask.toggle().completed());
    }

    @Test
    void taskFiltersShowCorrectTasks() {
        App.Task openTask = new App.Task(
                1,
                "Opening checklist",
                "Boca Raton",
                App.Priority.NORMAL,
                false,
                LocalDateTime.now()
        );

        App.Task completedTask = new App.Task(
                2,
                "Check sanitizer",
                "Tallahassee",
                App.Priority.HIGH,
                true,
                LocalDateTime.now()
        );

        assertTrue(App.TaskFilter.ALL.includes(openTask));
        assertTrue(App.TaskFilter.ALL.includes(completedTask));

        assertTrue(App.TaskFilter.OPEN.includes(openTask));
        assertFalse(App.TaskFilter.OPEN.includes(completedTask));

        assertFalse(App.TaskFilter.COMPLETED.includes(openTask));
        assertTrue(App.TaskFilter.COMPLETED.includes(completedTask));
    }

    @Test
    void filterIsReadFromUrl() {
        assertEquals(
                App.TaskFilter.OPEN,
                App.TaskFilter.from(URI.create("/?filter=open"))
        );

        assertEquals(
                App.TaskFilter.COMPLETED,
                App.TaskFilter.from(URI.create("/?filter=completed"))
        );

        assertEquals(
                App.TaskFilter.ALL,
                App.TaskFilter.from(URI.create("/?filter=wrong"))
        );
    }
}
