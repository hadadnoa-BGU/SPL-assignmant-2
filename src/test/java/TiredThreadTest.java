import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import scheduling.TiredThread;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class TiredThreadTest {
    private TiredThread thread1;
    private TiredThread thread2;
    private TiredThread thread3;
    private TiredThread thread4;
    private Runnable task1;

     @BeforeEach
    void setUp() {
        thread1=new TiredThread(1,0.5);
        thread1.start();
        thread2=new TiredThread(2,0.4);
        thread2.start();
        thread3=new TiredThread(3,0.6);
        thread3.start();
        thread4=new TiredThread(4,1.2);
        thread4.start();
        task1=()->{
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
    }
    @Test
    void testCompareTo() {
        thread1.newTask(task1);
        thread2.newTask(task1);
        thread3.newTask(task1);
        thread4.newTask(task1);
        while (thread1.isBusy() || thread2.isBusy() || thread3.isBusy() || thread4.isBusy()) {
            //wait for all threads to finish their tasks
            
        }
        int result1=thread1.compareTo(thread2);
        assert(result1 == 1);
        int result2=thread2.compareTo(thread3);
        assert(result2 == -1);
        int result3=thread3.compareTo(thread4);
        assert(result3 == -1); 
    }

    @Test
    void testNewTask() throws InterruptedException {
        AtomicBoolean ran = new AtomicBoolean(false);
        Runnable simpleTask = () -> {
            try { Thread.sleep(50); } catch (Exception e) {}
            ran.set(true);
        };

        // 1. Submit task
        thread1.newTask(simpleTask);
        
        // 2. Assert thread becomes busy (Task takes 50ms, so it should be busy immediately)
        assertTrue(thread1.isBusy(), "Thread should report busy after receiving task");

        // 3. Wait for finish
        while (thread1.isBusy()) {
            Thread.sleep(10);
        }

        // 4. Assert task actually ran
        assertTrue(ran.get(), "The task code should have executed");
    }

    @Test
    void testRun() throws InterruptedException {
        // Verifies the loop mechanism by checking if fatigue increases over time
        double initialFatigue = thread1.getFatigue();
        assertEquals(0.0, initialFatigue, 0.001);

        // Run task
        thread1.newTask(task1);
        
        while (thread1.isBusy()) {
            Thread.sleep(10);
        }

        // Run another task
        thread1.newTask(task1);
        
        while (thread1.isBusy()) {
            Thread.sleep(10);
        }

        // Assert fatigue accumulated
        assertTrue(thread1.getFatigue() > 0, "Fatigue should be greater than 0 after running tasks");
        assertTrue(thread1.getTimeUsed() > 0, "TimeUsed should be recorded");
    }

    @Test
    void testShutdown() throws InterruptedException {
        // 1. Call shutdown
        thread1.shutdown();

        // 2. Verify we cannot add new tasks
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            thread1.newTask(() -> {});
        });
        assertEquals("Worker is shutting down", exception.getMessage());

        // 3. Verify the thread actually dies (Java thread terminates)
        // thread.join() waits for the thread to die. If it takes > 2000ms, something is wrong.
        thread1.join(2000); 
        
        assertFalse(thread1.isAlive(), "Thread should be dead (not alive) after shutdown processes");
    }
}