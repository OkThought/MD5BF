package ru.ccfit.nsu.bogush.md5bf.server;

import ru.ccfit.nsu.bogush.md5bf.ConnectionRequestType;
import ru.ccfit.nsu.bogush.md5bf.bf.Task;
import ru.ccfit.nsu.bogush.md5bf.bf.TaskCreator;
import ru.ccfit.nsu.bogush.md5bf.net.SocketReader;
import ru.ccfit.nsu.bogush.md5bf.net.SocketWriter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.ALPHABET;
import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.CHARSET;
import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.PROTOCOL;

public class Server extends Thread {
    private static final int EXIT_FAILURE = 1;
    private static final int TASK_QUEUE_SIZE = 16;
    private static final int MAX_SEQUENCE_LENGTH = 16;
    private static final long INDEX_STEP = 1024;
    private static final int REQUIRED_NUMBER_OF_ARGUMENTS = 2;
    private static final int HASH_ARGUMENT_INDEX = 0;
    private static final int PORT_ARGUMENT_INDEX = 1;
    private static final long TASK_TIMEOUT = 1000; // millis
    private ServerSocket serverSocket;
    private TaskCreator taskCreator;
    private LinkedBlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>(TASK_QUEUE_SIZE);
    private ConcurrentHashMap<UUID, AssignedTask> assignedTasks = new ConcurrentHashMap<>();
    private TaskTimeoutGuard taskTimeoutGuard = new TaskTimeoutGuard();

    public Server(String md5hash, int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        taskCreator = new TaskCreator(taskQueue, INDEX_STEP, MAX_SEQUENCE_LENGTH, md5hash.getBytes(CHARSET), ALPHABET);
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

    @Override
    public synchronized void start() {
        taskCreator.start();
        super.start();
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
            SocketWriter socketWriter;
            try {
                socketReader = new SocketReader(clientSocket.getInputStream());
                socketWriter = new SocketWriter(clientSocket.getOutputStream());

            } catch (IOException e) {
                System.err.println("Server: couldn't get i/o streams of the client socket");
                try {
                    clientSocket.close();
                } catch (IOException e1) {
                    System.err.println("Server FATAL: Couldn't close client socket");
                    System.exit(EXIT_FAILURE);
                }
                continue;
            }

            System.err.println("Client connecting");

            if (!recognizeClientProtocol(socketReader)) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Couldn't close client socket");
                }
                continue;
            }

            ConnectionRequestType type;
            try {
                type = ConnectionRequestType.forByte(socketReader.readByte());
                System.err.println("Received connection request type " + type);
            } catch (IOException e) {
                System.err.println("Couldn't read connection request type");
                return;
            }

            UUID uuid = readClientUUID(socketReader);
            if (uuid == null) return;

            switch (type) {
                case TASK_REQUEST:
                    System.err.println("Received TASK_REQUEST from client " + uuid);
                    handleTaskRequest(socketWriter, uuid);
                    break;
                case TASK_DONE:
                    System.err.println("Received TASK_DONE from client " + uuid);
                    handleTaskDone(socketReader);
                    break;
                default:
                    System.err.println("Unknown client connection request type detected");
                    return;
            }

            try {
                clientSocket.getOutputStream().flush();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!taskTimeoutGuard.isAlive()) {
                taskTimeoutGuard.start();
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Couldn't close server socket");
            e.printStackTrace();
        }
    }

    private boolean recognizeClientProtocol(SocketReader socketReader) {
        try {
            String protocol = socketReader.readString(CHARSET, PROTOCOL.length());
            if (!protocol.equals(PROTOCOL)) {
                System.err.println("The protocol '" + protocol + "' is not recognised");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Couldn't read protocol details");
            return false;
        }
        System.err.println("Protocol details recognised");
        return true;
    }

    private UUID readClientUUID(SocketReader socketReader) {
        try {
            return socketReader.readUUID(CHARSET);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Couldn't read uuid");
        }
        return null;
    }

    private void handleTaskRequest(SocketWriter socketWriter, UUID uuid) {
        Task task = null;
        try {
            task = taskQueue.take();
        } catch (InterruptedException e) {
            System.err.println("Interrupted while taking task from queue");
            return;
        }

        assignedTasks.put(uuid, new AssignedTask(task, System.currentTimeMillis() + TASK_TIMEOUT));

        try {
            socketWriter.writeTask(task);
        } catch (IOException e) {
            System.err.println("Couldn't write task");
        }
    }

    private void handleTaskDone(SocketReader socketReader) {
        String secretString;
        try {
            secretString = socketReader.readString(CHARSET);
        } catch (IOException e) {
            System.err.println("Couldn't read secret string");
            return;
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Couldn't close server socket!");
            System.exit(EXIT_FAILURE);
        }

        System.out.println(secretString);
        interrupt();
    }

    @Override
    public void interrupt() {
        taskCreator.interrupt();
        super.interrupt();
    }

    private static class AssignedTask {
        private Task task;
        private long timeLimit;

        private AssignedTask(Task task, long timeLimit) {
            this.task = task;
            this.timeLimit = timeLimit;
        }
    }

    private class TaskTimeoutGuard extends Thread {
        private TaskTimeoutGuard() {
            super("Task Timeout Guard");
        }

        @Override
        public void run() {
            assignedTasks.forEach((uuid, assignedTask) -> {
                if (System.currentTimeMillis() > assignedTask.timeLimit) {
                    assignedTasks.remove(uuid);
                    System.err.println("Client " + uuid + " task timed out");
                    try {
                        taskQueue.put(assignedTask.task);
                    } catch (InterruptedException e) {
                        System.err.println(getName() + ": Interrupted on task put in queue");
                    }
                }
            });
        }
    }
}
