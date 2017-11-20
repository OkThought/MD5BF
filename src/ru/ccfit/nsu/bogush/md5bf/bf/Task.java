package ru.ccfit.nsu.bogush.md5bf.bf;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;

public class Task implements Serializable {
    public long sequenceStartIndex;
    public long sequenceFinishIndex;
    public byte[] hash;
    public String alphabet;

    public Task(long sequenceStartIndex, long sequenceFinishIndex, byte[] hash, String alphabet) {
        this.sequenceStartIndex = sequenceStartIndex;
        this.sequenceFinishIndex = sequenceFinishIndex;
        this.hash = hash;
        this.alphabet = alphabet;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeInt(hash.length);
        out.write(hash);
        out.writeInt(alphabet.length());
        out.writeChars(alphabet);
        out.writeLong(sequenceStartIndex);
        out.writeLong(sequenceFinishIndex);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        hash = new byte[in.readInt()];
        in.readFully(hash);

        char[] alphabetChars = new char[in.readInt()];
        for (int i = 0; i < alphabetChars.length; ++i) {
            alphabetChars[i] = in.readChar();
        }
        this.alphabet = new String(alphabetChars);

        sequenceStartIndex = in.readLong();
        sequenceFinishIndex = in.readLong();
    }

    private void readObjectNoData()
            throws ObjectStreamException {
        this.alphabet = null;
    }

    @Override
    public String toString() {
        return "Task{" +
                "sequenceStartIndex=" + sequenceStartIndex +
                ", sequenceFinishIndex=" + sequenceFinishIndex +
                ", hash=" + Arrays.toString(hash) +
                ", alphabet='" + alphabet + '\'' +
                '}';
    }
}