package ru.ccfit.nsu.bogush.md5bf.bf;

import java.util.concurrent.BlockingQueue;

public class TaskCreator extends Thread {
    private BlockingQueue<Task> taskQueue;
    private int maxSequenceLength;
    private long indexStep;
    private byte[] hash;
    private String alphabet;
    private TaskCreatorListener taskCreatorListener;

    public TaskCreator(BlockingQueue<Task> taskQueue, int maxSequenceLength, byte[] hash, String alphabet) {
        super("Task Creator");
        this.taskQueue = taskQueue;
        this.maxSequenceLength = maxSequenceLength;
        this.indexStep = indexStep;
        this.hash = hash;
        this.alphabet = alphabet;
    }

    public void setTaskCreatorListener(TaskCreatorListener taskCreatorListener) {
        this.taskCreatorListener = taskCreatorListener;
    }

    @Override
    public void run() {
//        try {
//            TODO: create and put tasks in taskQueue
//        } catch (InterruptedException e) {
//            System.err.println("Task Creator interrupted");
//        }
        if (taskCreatorListener != null) {
            taskCreatorListener.tasksFinished();
        }
    }

    public interface TaskCreatorListener {
        void tasksFinished();
    }
}
