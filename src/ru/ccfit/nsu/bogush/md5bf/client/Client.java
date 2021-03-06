package ru.ccfit.nsu.bogush.md5bf.client;

import ru.ccfit.nsu.bogush.md5bf.ConnectionRequestType;
import ru.ccfit.nsu.bogush.md5bf.bf.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import static ru.ccfit.nsu.bogush.md5bf.ConnectionRequestType.TASK_DONE;
import static ru.ccfit.nsu.bogush.md5bf.ConnectionRequestType.TASK_REQUEST;
import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.CHARSET;
import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.PROTOCOL;

public class Client extends Thread {
    private static final int REQUIRED_NUMBER_OF_ARGUMENTS = 2;
    private static final int EXIT_FAILURE = 1;
    private static final int ADDRESS_ARGUMENT_INDEX = 0;
    private static final int PORT_ARGUMENT_INDEX = 1;
    private static final int CONNECTION_RETRIES_NUMBER = 5;
    private static final int CONNECTION_TIMEOUT = 3000; // millis
    private static final int CURRENT_TEST_STRING_LOG_RATE_MAGNIFIER = 1000;
    private static MessageDigest MD5;

    static {
        try {
            MD5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
    }

    private final InetAddress serverAddress;
    private final int serverPort;
    private final UUID uuid;
    private Socket socket;

    private Client(InetAddress serverAddress, int serverPort) throws IOException {
        super("Client");
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.uuid = UUID.randomUUID();
        System.err.println("Starting MD5BF Client " + uuid);
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
            System.err.println("Couldn't start the Client");
            e.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
    }

    @Override
    public void run() {
        int connectionRetriesLeft = CONNECTION_RETRIES_NUMBER;
        String secretString = null;
        while (!Thread.interrupted()) {
            ObjectInputStream in;
            ObjectOutputStream out;
            socket = new Socket();
            try {
                System.err.println("Connecting to server " + serverAddress + ":" + serverPort);
                socket.connect(new InetSocketAddress(serverAddress, serverPort), CONNECTION_TIMEOUT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                connectionRetriesLeft = CONNECTION_RETRIES_NUMBER;
                System.err.println("Connected");
            } catch (SocketTimeoutException e) {
                System.err.println("Connection timed out");
                if (--connectionRetriesLeft >= 0) {
                    System.err.println("Connection retries left: " + connectionRetriesLeft);
                } else {
                    return;
                }
                continue;
            } catch (IOException e) {
                System.err.println("Couldn't connect to server");
                break;
            }

            try {
                System.err.println("Send protocol details: '" + PROTOCOL + '\'');
                out.writeObject(PROTOCOL);
            } catch (IOException e) {
                System.err.println("Couldn't send protocol details");
                break;
            }

            try {
                System.err.println("Send uuid");
                out.writeObject(uuid);
            } catch (IOException e) {
                System.err.println("Couldn't send uuid");
                break;
            }

            ConnectionRequestType connectionRequestType = secretString == null ? TASK_REQUEST : TASK_DONE;
            try {
                System.err.println("Send " + connectionRequestType);
                out.writeByte(connectionRequestType.toByte());
            } catch (IOException e) {
                System.err.println("Couldn't send " + connectionRequestType);
                break;
            }

            if (secretString != null) {
                try {
                    System.err.println("Send secret string: \"" + secretString + "\"");
                    out.writeObject(secretString);
                } catch (IOException e) {
                    System.err.println("Couldn't write secret string");
                }
                break;
            }

            try {
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Task task;
            try {
                System.err.println("Receive task");
                task = (Task) in.readObject();
                System.err.println("Received task: " + task);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Couldn't read task");
                break;
            }

            try {
                System.err.println("Close connection");
                socket.getOutputStream().flush();
                socket.close();
            } catch (IOException e) {
                System.err.println("Couldn't close socket");
                break;
            }

            secretString = processTask(task);
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
        int i = 0;
        int maxLen = task.finish().length();
        int logRate = maxLen * CURRENT_TEST_STRING_LOG_RATE_MAGNIFIER;
        for (String testString : task) {
            byte[] hash = MD5.digest(testString.getBytes(CHARSET));
            if (i % logRate == 0) {
                System.err.println("Current test string is: \"" + testString + '"');
            }
            if (Arrays.equals(hash, task.hash())) {
                return testString;
            }
            ++i;
        }
        return null;
    }
}
