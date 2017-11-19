package ru.ccfit.nsu.bogush.md5bf;

import java.util.concurrent.BlockingQueue;

public class TaskCreator extends Thread {
    private BlockingQueue<Task> taskQueue;
    private long maxSequenceIndex;
    private long indexStep;
    private byte[] hash;
    private String alphabet;

    public TaskCreator(BlockingQueue<Task> taskQueue, long indexStep, int maxSequenceLength, byte[] hash, String alphabet) {
        super("Task Creator");
        this.taskQueue = taskQueue;
        this.indexStep = indexStep;
        this.hash = hash;
        this.maxSequenceIndex = SymbolSequenceCalculator.numberOfSequences(maxSequenceLength, alphabet.length());
        this.alphabet = alphabet;
    }

    @Override
    public void run() {
        try {
            long index = 0;
            while (index < maxSequenceIndex) {
                taskQueue.put(new Task(index, Math.min(index + indexStep, maxSequenceIndex), hash, alphabet));
                index += indexStep;
            }
        } catch (InterruptedException e) {
            System.err.println("Task Creator interrupted");
        }
    }
}
