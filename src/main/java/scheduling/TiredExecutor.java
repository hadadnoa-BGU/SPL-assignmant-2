package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // TODO
        this.workers = new TiredThread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            // some random fatige factor
            double fatigue = 0.5 + (1 / (double)numThreads) * i;

            TiredThread w = new TiredThread(i, fatigue);
            workers[i] = w;
            idleMinHeap.add(w);
            w.start();
        }
    }

    public void submit(Runnable task) {
        // TODO
        if (task == null) 
            throw new IllegalArgumentException("task is null");

        try 
        {
            final TiredThread worker = idleMinHeap.take();
            inFlight.incrementAndGet();

            Runnable wrapped = () -> {
                try
                {
                    task.run();
                } 
                finally 
                {
                    inFlight.decrementAndGet();
                    idleMinHeap.add(worker); // reinsert after fatigue/timeUsed updated
                    synchronized (TiredExecutor.this) 
                    {
                        TiredExecutor.this.notifyAll();
                    }
                }
            };

            worker.newTask(wrapped);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("submit interrupted", e);
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        for(Runnable task : tasks)
        {
            submit(task);
        }
        synchronized (this) {
            while (inFlight.get() != 0) 
            {
                try 
                {
                    wait();
                } 
                catch (InterruptedException e) 
                {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("submitAll interrupted", e);
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
        synchronized (this) {
            while (inFlight.get() != 0) 
            {
                try 
                {
                    wait();
                } 
                catch (InterruptedException e) 
                {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("submitAll interrupted", e);
                }
            }
        }
    }

    public synchronized String getWorkerReport() 
    {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Worker Report ===\n");
        sb.append("inFlight=").append(inFlight.get())
        .append(", idle=").append(idleMinHeap.size())
        .append(", total=").append(workers.length)
        .append("\n");


        double minFatigue = Double.MAX_VALUE;
        double maxFatigue = Double.MIN_VALUE;


        for (TiredThread w : workers) 
        {
            sb.append("Worker ")
            .append(w.getWorkerId())
            .append(" [").append(w.getName()).append("]")
            .append(" busy=").append(w.isBusy())
            .append(" used(ns)=").append(w.getTimeUsed())
            .append(" idle(ns)=").append(w.getTimeIdle())
            .append(" Fatigue=").append(w.getFatigue())
            .append("\n");
        }

        double fairnessScore;
        if (workers.length > 0) {
            fairnessScore = maxFatigue - minFatigue;
        } else {
            fairnessScore = 0.0;
        }

        sb.append("Fairness=").append(fairnessScore).append("\n");

        return sb.toString();
    }
}
