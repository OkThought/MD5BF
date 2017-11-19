package ru.ccfit.nsu.bogush.md5bf;

import sun.jvm.hotspot.debugger.cdbg.Sym;

import java.io.IOException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.CHARSET;
import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.PROTOCOL;

public class Client extends Thread {
    private static final int REQUIRED_NUMBER_OF_ARGUMENTS = 2;
    private static final int EXIT_FAILURE = 1;
    private static final int ADDRESS_ARGUMENT_INDEX = 0;
    private static final int PORT_ARGUMENT_INDEX = 1;
    private static MessageDigest MD5;

    static {
        try {
            MD5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
    }

    private final Socket socket;
    private final SocketReader reader;
    private final SocketWriter writer;
    private final InetAddress serverAddress;
    private final int serverPort;
    private final UUID uuid;

    public Client(InetAddress serverAddress, int serverPort) throws IOException {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.socket = new Socket(serverAddress, serverPort);
        this.reader = new SocketReader(socket.getInputStream());
        this.writer = new SocketWriter(socket.getOutputStream());
        this.uuid = UUID.randomUUID();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            if (!socket.isConnected()) {
                try {
                    socket.connect(new InetSocketAddress(serverAddress, serverPort));
                } catch (IOException e) {
                    System.err.println("Couldn't connect to server");
                    break;
                }
            }

            try {
                writer.writeString(PROTOCOL, CHARSET);
            } catch (IOException e) {
                System.err.println("Couldn't write protocol");
                break;
            }

            try {
                writer.writeUUID(uuid, CHARSET);
            } catch (IOException e) {
                System.err.println("Couldn't write uuid");
                break;
            }

            try {
                writer.writeByte(ConnectionRequestType.TASK_REQUEST.toByte());
            } catch (IOException e) {
                System.err.println("Couldn't write connection request type");
                break;
            }

            Task task;
            try {
                task = reader.readTask();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Couldn't read task");
                break;
            }

            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Couldn't close socket");
                break;
            }

            String secretString = processTask(task);
            if (secretString != null) {
                try {
                    socket.connect(new InetSocketAddress(serverAddress, serverPort));
                } catch (IOException e) {
                    System.err.println("Couldn't connect to server");
                    break;
                }

                try {
                    writer.writeString(PROTOCOL, CHARSET);
                } catch (IOException e) {
                    System.err.println("Couldn't write protocol");
                    break;
                }

                try {
                    writer.writeUUID(uuid, CHARSET);
                } catch (IOException e) {
                    System.err.println("Couldn't write uuid");
                    break;
                }

                try {
                    writer.writeByte(ConnectionRequestType.TASK_DONE.toByte());
                } catch (IOException e) {
                    System.err.println("Couldn't write connection request type");
                    break;
                }

                try {
                    writer.writeString(secretString, CHARSET);
                } catch (IOException e) {
                    System.err.println("Couldn't write secret string");
                    break;
                }
                break;
            }
        }

        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String processTask(Task task) {
        for (long i = task.sequenceStartIndex; i < task.sequenceFinishIndex; i++) {
            String secretString = SymbolSequenceCalculator.stringFromSequenceIndex(i, task.alphabet);
            if (Arrays.equals(MD5.digest(secretString.getBytes(CHARSET)), task.hash)) {
                return secretString;
            }
        }
        return null;
    }

    private static void usage() {
        System.out.print("Usage\n\t");
        System.out.print("java -jar client.jar address port\n\n");
        System.out.print("Description\n\t");
        System.out.print("A client MD5BF program to brute-force in parallel tasks received\n\t" +
                "from server.\n\n");
        System.out.print("Parameters\n\t");
        System.out.print("address - ipv4 or ipv6 address or domain name of the server.\n\n\t");
        System.out.print("port - port of the server.\n\n");
    }

    public static void main(String[] args) {
        if (args.length != REQUIRED_NUMBER_OF_ARGUMENTS) {
            usage();
            System.exit(EXIT_FAILURE);
        }

        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getByName(args[ADDRESS_ARGUMENT_INDEX]);
        } catch (UnknownHostException e) {
            System.err.println("Couldn't get address by name: " + args[ADDRESS_ARGUMENT_INDEX]);
            usage();
            System.exit(EXIT_FAILURE);
        }

        int serverPort = 0;
        try {
            serverPort = Integer.parseInt(args[PORT_ARGUMENT_INDEX]);
        } catch (NumberFormatException e) {
            System.err.println("Couldn't parse serverPort number!");
            usage();
            System.exit(EXIT_FAILURE);
        }

        try {
            new Client(serverAddress, serverPort).start();
        } catch (IOException e) {
            System.err.println("Couldn't start the Server");
            System.exit(EXIT_FAILURE);
        }
    }
}
