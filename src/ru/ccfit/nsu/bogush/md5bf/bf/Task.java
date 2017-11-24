package ru.ccfit.nsu.bogush.md5bf.bf;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;

public class Task implements Iterator<String>, Iterable<String>, Serializable {
    private byte[] hash;
    private String alphabet;
    private String start;
    private String finish;
    private SymbolSequenceIterator symbolSequenceIterator;

    public Task(byte[] hash, char[] alphabet, char[] start, char[] finish) {
        this.hash = hash;
        this.alphabet = String.valueOf(alphabet);
        this.start = String.valueOf(start);
        this.finish = String.valueOf(finish);
        this.symbolSequenceIterator = new SymbolSequenceIterator(alphabet, start, finish);
    }

    public String alphabet() {
        return alphabet;
    }

    public String start() {
        return start;
    }

    public String finish() {
        return finish;
    }

    public byte[] hash() {
        return hash;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeInt(hash.length);
        out.write(hash);
        writeString(out, alphabet);
        writeString(out, start);
        writeString(out, finish);
    }

    private void writeString(java.io.ObjectOutputStream out, String string) throws IOException {
        out.writeInt(string.length());
        out.writeChars(string);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.readFully(hash = new byte[in.readInt()]);
        alphabet = readString(in);
        start = readString(in);
        finish = readString(in);
        symbolSequenceIterator = new SymbolSequenceIterator(
                alphabet.toCharArray(), start.toCharArray(), finish.toCharArray());
    }

    private String readString(java.io.ObjectInputStream in) throws IOException {
        char[] chars = new char[in.readInt()];
        for (int i = 0; i < chars.length; ++i) {
            chars[i] = in.readChar();
        }
        return String.valueOf(chars);
    }

    private void readObjectNoData()
            throws ObjectStreamException {
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return symbolSequenceIterator.hasNext();
    }

    @Override
    public String next() {
        return new String(symbolSequenceIterator.next());
    }
}
