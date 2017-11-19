package ru.ccfit.nsu.bogush.md5bf;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class Task implements Serializable {
    long sequenceStartIndex;
    long sequenceFinishIndex;
    String alphabet;

    public Task(long sequenceStartIndex, long sequenceFinishIndex, String alphabet) {
        this.sequenceStartIndex = sequenceStartIndex;
        this.sequenceFinishIndex = sequenceFinishIndex;
        this.alphabet = alphabet;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeInt(alphabet.length());
        out.writeChars(alphabet);
        out.writeLong(sequenceStartIndex);
        out.writeLong(sequenceFinishIndex);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        char[] alphabetChars = new char[in.readInt()];
        for (int i = 0; i < alphabetChars.length; ++i) {
            alphabetChars[i] = in.readChar();
        }
        sequenceStartIndex = in.readLong();
        sequenceFinishIndex = in.readLong();
    }
    private void readObjectNoData()
            throws ObjectStreamException {
        this.alphabet = null;
    }
}
