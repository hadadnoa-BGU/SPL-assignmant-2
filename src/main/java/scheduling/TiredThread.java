package scheduling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TiredThread extends Thread implements Comparable<TiredThread> {

    private static final Runnable POISON_PILL = () -> {}; // Special task to signal shutdown

    private final int id; // Worker index assigned by the executor
    private final double fatigueFactor; // Multiplier for fatigue calculation

    private final AtomicBoolean alive = new AtomicBoolean(true); // Indicates if the worker should keep running

    // Single-slot handoff queue; executor will put tasks here
    private final BlockingQueue<Runnable> handoff = new ArrayBlockingQueue<>(1);

    private final AtomicBoolean busy = new AtomicBoolean(false); // Indicates if the worker is currently executing a task

    private final AtomicLong timeUsed = new AtomicLong(0); // Total time spent executing tasks
    private final AtomicLong timeIdle = new AtomicLong(0); // Total time spent idle
    private final AtomicLong idleStartTime = new AtomicLong(0); // Timestamp when the worker became idle

    public TiredThread(int id, double fatigueFactor) {
        this.id = id;
        this.fatigueFactor = fatigueFactor;
        this.idleStartTime.set(System.nanoTime());
        setName(String.format("FF=%.2f", fatigueFactor));
    }

    public int getWorkerId() {
        return id;
    }

    public double getFatigue() {
        return fatigueFactor * timeUsed.get();
    }

    public boolean isBusy() {
        return busy.get();
    }

    public long getTimeUsed() {
        return timeUsed.get();
    }

    public long getTimeIdle() {
        return timeIdle.get();
    }

    /**
     * Assign a task to this worker.
     * This method is non-blocking: if the worker is not ready to accept a task,
     * it throws IllegalStateException.
     */
    public void newTask(Runnable task) 
    {
        // TODO

        if (!alive.get()) {
            throw new IllegalStateException("Worker is shutting down");
        }

        if (!handoff.offer(task)) 
        {
            throw new IllegalStateException("Worker " + id + " is not ready to accept a task");
        }
    }

    /**
     * Request this worker to stop after finishing current task.
     * Inserts a poison pill so the worker wakes up and exits.
     */
    public void shutdown() {
        // TODO
       alive.set(false);
        handoff.offer(POISON_PILL);
    }

    @Override
    public void run() {
        // TODO

        while (alive.get()) {
            Runnable task;
            try {
                task = handoff.take();
            } catch (InterruptedException e) {
                continue;
            }

            // calculates how long it was idle
            long now = System.nanoTime();
            long idleDuration = now - idleStartTime.get();
            timeIdle.addAndGet(idleDuration);

            // checks shutdown
            if (task == POISON_PILL) break;

            // executes after marking as busy
            busy.set(true);
            long startWork = System.nanoTime();
            try {
                task.run();
            } catch (RuntimeException e) {
                e.printStackTrace(); // Prevent thread death on task failure
            } finally {
                // updates fatigue and work
                long duration = System.nanoTime() - startWork;
                timeUsed.addAndGet(duration);

                // free
                busy.set(false);
                idleStartTime.set(System.nanoTime());
            }
        }
    }

    @Override
    public int compareTo(TiredThread o) {
        // TODO
        int c = Double.compare(this.getFatigue(), o.getFatigue());
        if (c != 0) return c;
        return Integer.compare(this.id, o.id); // tie breaker if this.getFatigue() == o.getFatigue()
    }
}