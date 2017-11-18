package ru.ccfit.nsu.bogush.md5bf;

public class Server {
    private static final int EXIT_FAILURE = 1;
    private static int REQUIRED_NUMBER_OF_ARGUMENTS = 2;
    private static int HASH_ARGUMENT_INDEX = 0;
    private static int PORT_ARGUMENT_INDEX = 1;

    private static void usage() {
        System.out.print("Usage\n\t");
        System.out.print("java -jar md5bf.jar hash port\n\n");
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
    }
}
