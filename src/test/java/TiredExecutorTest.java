import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import scheduling.TiredExecutor;

public class TiredExecutorTest {
    private TiredExecutor executor;
    private final int NUM_THREADS = 4;

    @BeforeEach
    void setUp() {
        // Initialize with a fixed number of threads before each test
        executor = new TiredExecutor(NUM_THREADS);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        // Ensure threads are cleaned up after tests
        executor.shutdown();
    }

    /**
     * Test that a single task can be submitted and executed.
     * This verifies that worker threads are started correctly.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS) // Fail if it hangs (deadlock check)
    void testSingleTaskExecution() {
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();
        
        tasks.add(() -> counter.incrementAndGet());

        executor.submitAll(tasks);

        assertEquals(1, counter.get(), "Counter should be incremented to 1");
    }

    /**
     * Test that submitAll blocks until ALL tasks are finished.
     * If submitAll returns before tasks are done, this test will fail.
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testSubmitAllBlocksUntilCompletion() {
        int taskCount = 10;
        AtomicInteger completedTasks = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < taskCount; i++) {
            tasks.add(() -> {
                try {
                    // Simulate work to ensure the main thread has to wait
                    Thread.sleep(50); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                completedTasks.incrementAndGet();
            });
        }

        // This method should BLOCK until all 10 tasks are done
        executor.submitAll(tasks);

        // If we reach here, tasks should be 100% finished
        assertEquals(taskCount, completedTasks.get(), "submitAll returned before all tasks were finished!");
    }

    /**
     * Test that the executor handles more tasks than threads.
     * This verifies the queueing logic (idleMinHeap).
     */
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testMoreTasksThanThreads() {
        int taskCount = NUM_THREADS * 5; // e.g., 20 tasks for 4 threads
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < taskCount; i++) {
            tasks.add(counter::incrementAndGet);
        }

        executor.submitAll(tasks);

        assertEquals(taskCount, counter.get(), "All tasks should be executed eventually");
    }

    /**
     * Test that the worker report returns a non-empty string.
     */
    @Test
    void testWorkerReportGeneration() {
        // Run at least one task so stats are generated
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {});
        executor.submitAll(tasks);

        String report = executor.getWorkerReport();
        
        assertNotNull(report);
        assertTrue(report.contains("Worker Report"), "Report should contain header");
        assertTrue(report.contains("Fatigue"), "Report should contain fatigue stats");
        assertTrue(report.contains("Fairness"), "Report should contain fairness calculation");
    }
}
