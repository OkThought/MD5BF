package ru.ccfit.nsu.bogush.md5bf;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.UUID;

import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.PROTOCOL;

public class Server extends Thread {
    private static final int EXIT_FAILURE = 1;
    private static int REQUIRED_NUMBER_OF_ARGUMENTS = 2;
    private static int HASH_ARGUMENT_INDEX = 0;
    private static int PORT_ARGUMENT_INDEX = 1;
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private final String md5hash;
    private ServerSocket serverSocket;

    public Server(String md5hash, int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.md5hash = md5hash;
    }

    @Override
    public void run() {
        int client_counter = 0;
        while (!Thread.interrupted()) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Server: An I/O error occurs when waiting for a connection.");
                continue;
            }

            SocketReader socketReader;
            try {
                socketReader = new SocketReader(clientSocket.getInputStream());
            } catch (IOException e) {
                System.err.println("Server: couldn't get input stream of the client socket");
                continue;
            }

            SocketWriter socketWriter;
            try {
                socketWriter = new SocketWriter(clientSocket.getOutputStream());
            } catch (IOException e) {
                System.err.println("Server: couldn't get output stream of the client socket");
                continue;
            }

            new Thread(() -> {
                // TODO: 11/19/17 Expand this lambda into public class
                // TODO: 11/19/17 Add public class' name before each message to console
                // TODO: 11/19/17 Close io streams and socket before finishing
                try {
                    if (!socketReader.readString(CHARSET, PROTOCOL.length()).equals(PROTOCOL)) {
                        System.err.println("The " + this.getName() + " uses unknown protocol");
                        return;
                    }
                } catch (IOException e) {
                    System.err.println("Couldn't read protocol");
                    return;
                }

                ClientState state;
                try {
                    state = ClientState.forByte(socketReader.readByte());
                } catch (IOException e) {
                    System.err.println("Couldn't read state");
                    return;
                }

                switch (state) {
                    case UNKNOWN:
                        System.err.println("Unknown client state detected");
                        return;
                    case TASK_REQUEST:
                        try {
                            UUID uuid = socketReader.readUUID();
                            setName("Client-" + uuid);
                            System.err.println("Established incoming TASK_REQUEST connection from " + getName());
                        } catch (IOException e) {
                            System.err.println("Couldn't read uuid");
                            return;
                        }
                        break;
                    case TASK_DONE:
                        String secretString;
                        try {
                            secretString = socketReader.readString(CHARSET);
                        } catch (IOException e) {
                            System.err.println("Couldn't read secret string");
                            return;
                        }

                        System.out.println(secretString);
                }
            }, "Client-" + client_counter++).start();
        }
    }

    private static void usage() {
        System.out.print("Usage\n\t");
        System.out.print("java -jar server.jar hash port\n\n");
        System.out.print("Description\n\t");
        System.out.print("A server to send brute-force tasks to client programs that will\n\t" +
                "brute-force in parallel.\n\n");
        System.out.print("Parameters\n\t");
        System.out.print("hash - md5 hash of the string to hack.\n\n\t");
        System.out.print("port - port on which to listen to tcp connections from clients.\n\n");
    }

    public static void main(String[] args) {
        if (args.length != REQUIRED_NUMBER_OF_ARGUMENTS) {
            usage();
            System.exit(EXIT_FAILURE);
        }

        String md5hash = args[HASH_ARGUMENT_INDEX];
        int port = 0;
        try {
            port = Integer.parseInt(args[PORT_ARGUMENT_INDEX]);
        } catch (NumberFormatException e) {
            System.err.println("Couldn't parse port number!");
            usage();
            System.exit(EXIT_FAILURE);
        }

        try {
            new Server(md5hash, port).start();
        } catch (IOException e) {
            System.err.println("Couldn't start the Server");
            System.exit(EXIT_FAILURE);
        }
    }
}
