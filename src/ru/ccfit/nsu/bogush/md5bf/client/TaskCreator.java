package ru.ccfit.nsu.bogush.md5bf.client;

import ru.ccfit.nsu.bogush.md5bf.bf.SymbolSequenceIterator;
import ru.ccfit.nsu.bogush.md5bf.bf.Task;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

public class TaskCreator extends Thread {
    private BlockingQueue<Task> taskQueue;
    private int maxSequenceLength;
    private long indexStep;
    private byte[] hash;
    private char[] suffix;
    private TaskCreatorListener taskCreatorListener;
    private SymbolSequenceIterator symbolSequenceIterator;
    private char[] alphabet;


    public TaskCreator(byte[] hash, BlockingQueue<Task> taskQueue, String alphabet, int suffixLength, int maxSequenceLength) {
        super("Task Creator");
        this.taskQueue = taskQueue;
        this.indexStep = indexStep;
        this.hash = hash;
        this.alphabet = alphabet.toCharArray();
        this.suffix = new char[suffixLength];
        Arrays.fill(suffix, this.alphabet[this.alphabet.length-1]);
        symbolSequenceIterator = new SymbolSequenceIterator(this.alphabet, maxSequenceLength);
    }

    public void setTaskCreatorListener(TaskCreatorListener taskCreatorListener) {
        this.taskCreatorListener = taskCreatorListener;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                char[] lastSequence;
                for (char[] firstSequence; symbolSequenceIterator.hasNext();){
                    firstSequence = symbolSequenceIterator.next();
                    lastSequence = new char[firstSequence.length + suffix.length];
                    System.arraycopy(firstSequence, 0, lastSequence, 0, firstSequence.length);
                    System.arraycopy(suffix, 0, lastSequence, firstSequence.length, suffix.length);
                    taskQueue.put(new Task(hash, alphabet, firstSequence, lastSequence));
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Task Creator interrupted");
        }
        if (taskCreatorListener != null) {
            taskCreatorListener.tasksFinished();
        }
    }

    public interface TaskCreatorListener {
        void tasksFinished();
    }
}
