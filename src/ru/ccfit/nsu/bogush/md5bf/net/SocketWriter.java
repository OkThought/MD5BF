package ru.ccfit.nsu.bogush.md5bf.net;

import ru.ccfit.nsu.bogush.md5bf.bf.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class SocketWriter {
    private OutputStream outputStream;

    public SocketWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeByte(byte b) throws IOException {
        outputStream.write(b);
    }

    public void writeInt(int i) throws IOException {
        writeByte((byte) (i >> 24));
        writeByte((byte) (i >> 16));
        writeByte((byte) (i >> 8));
        writeByte((byte) i);
    }

    public void writeLong(long l) throws IOException {
        writeByte((byte) (l >> 56));
        writeByte((byte) (l >> 48));
        writeByte((byte) (l >> 40));
        writeByte((byte) (l >> 32));
        writeByte((byte) (l >> 24));
        writeByte((byte) (l >> 16));
        writeByte((byte) (l >> 8));
        writeByte((byte) l);
    }

    public void writeBytes(byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }

    public void writeUUID(UUID uuid, Charset charset) throws IOException {
        writeBytes(uuid.toString().getBytes(charset));
    }

    public void writeString(String string, Charset charset) throws IOException {
        writeInt(string.length());
        writeBytes(string.getBytes(charset));
    }

    public void writeTask(Task task) throws IOException {
        new ObjectOutputStream(outputStream).writeObject(task);
    }
}
