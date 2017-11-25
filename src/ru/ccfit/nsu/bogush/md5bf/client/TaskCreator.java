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
    private char[] firstSuffix;
    private char[] lastSuffix;
    private TaskCreatorListener taskCreatorListener;
    private SymbolSequenceIterator symbolSequenceIterator;
    private char[] alphabet;


    public TaskCreator(byte[] hash, BlockingQueue<Task> taskQueue, String alphabet, int suffixLength, int maxSequenceLength) {
        super("Task Creator");
        this.taskQueue = taskQueue;
        this.indexStep = indexStep;
        this.hash = hash;
        this.alphabet = alphabet.toCharArray();
        this.firstSuffix = new char[suffixLength];
        Arrays.fill(firstSuffix, this.alphabet[0]);
        this.lastSuffix = new char[suffixLength];
        Arrays.fill(lastSuffix, this.alphabet[this.alphabet.length - 1]);
        symbolSequenceIterator = new SymbolSequenceIterator(this.alphabet, maxSequenceLength);
    }

    static char[] concat(char[] a, char[] b) {
        if (a == null) return b;
        if (b == null) return a;
        char[] result = new char[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public void setTaskCreatorListener(TaskCreatorListener taskCreatorListener) {
        this.taskCreatorListener = taskCreatorListener;
    }

    @Override
    public void run() {
        if (!symbolSequenceIterator.hasNext()) return;
        try {
            char[] sequence = symbolSequenceIterator.next();
            char[] firstSequence = sequence;
            char[] lastSequence;
            while (symbolSequenceIterator.hasNext()) {
                lastSequence = concat(sequence, lastSuffix);
                taskQueue.put(new Task(hash, alphabet, firstSequence, lastSequence));
                firstSequence = concat(sequence, firstSuffix);
                sequence = symbolSequenceIterator.next();
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
