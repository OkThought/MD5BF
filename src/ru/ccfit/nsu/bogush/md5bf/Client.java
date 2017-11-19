package ru.ccfit.nsu.bogush.md5bf;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread {
    private static final int REQUIRED_NUMBER_OF_ARGUMENTS = 2;
    private static final int EXIT_FAILURE = 1;
    private static final int ADDRESS_ARGUMENT_INDEX = 0;
    private static final int PORT_ARGUMENT_INDEX = 1;
    private final Socket socket;

    public Client(InetAddress serverAddress, int serverPort) throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
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
