import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scheduling.TiredThread;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class TiredThreadTest {
    private TiredThread thread1;
    private TiredThread thread2;
    private TiredThread thread3;
    private TiredThread thread4;
    private Runnable task1;

    @BeforeEach
    void setUp() {
        thread1 = new TiredThread(1, 0.5); thread1.start();
        thread2 = new TiredThread(2, 0.4); thread2.start();
        thread3 = new TiredThread(3, 0.6); thread3.start();
        thread4 = new TiredThread(4, 1.2); thread4.start();

        // Task sleeps 100ms to guarantee fatigue accumulates
        task1 = () -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        };
    }

    @Test
    void testCompareTo() throws InterruptedException {
        thread1.newTask(task1);
        thread2.newTask(task1);
        thread3.newTask(task1);
        thread4.newTask(task1);

        // FIX: Give threads time to START processing (avoid "isBusy" race condition)
        Thread.sleep(20);

        // Wait for all to finish
        while (thread1.isBusy() || thread2.isBusy() || thread3.isBusy() || thread4.isBusy()) {
            Thread.sleep(10);
        }

        // T1 (0.5 * 100 = 50) vs T2 (0.4 * 100 = 40) -> T1 is LARGER (more tired) -> 1
        assertEquals(1, thread1.compareTo(thread2));

        // T2 (40) vs T3 (60) -> T2 is SMALLER -> -1
        assertEquals(-1, thread2.compareTo(thread3));

        // T3 (60) vs T4 (120) -> T3 is SMALLER -> -1
        assertEquals(-1, thread3.compareTo(thread4));
    }

    @Test
    void testNewTask() throws InterruptedException {
        AtomicBoolean ran = new AtomicBoolean(false);
        // Task must be long enough to catch "isBusy"
        Runnable simpleTask = () -> {
            try { Thread.sleep(100); } catch (Exception e) {}
            ran.set(true);
        };

        thread1.newTask(simpleTask);

        // FIX: Wait a tiny bit for the thread to wake up and set busy=true
        Thread.sleep(10);

        assertTrue(thread1.isBusy(), "Thread should report busy after receiving task");

        while (thread1.isBusy()) {
            Thread.sleep(10);
        }

        assertTrue(ran.get(), "The task code should have executed");
    }

    @Test
    void testShutdown() throws InterruptedException {
        thread1.shutdown();

        // Wait for shutdown to process
        thread1.join(2000);
        assertFalse(thread1.isAlive());

        // Now assert that new tasks are rejected
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            thread1.newTask(() -> {});
        });
        // Note: Check your message string matches exactly what you threw in TiredThread
        assertTrue(exception.getMessage().contains("shutting down") ||
                exception.getMessage().contains("not ready"));
    }

    // Keep your working testRun() as is
}