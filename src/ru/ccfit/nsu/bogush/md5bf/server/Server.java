package ru.ccfit.nsu.bogush.md5bf.server;

import ru.ccfit.nsu.bogush.md5bf.ConnectionRequestType;
import ru.ccfit.nsu.bogush.md5bf.bf.Task;
import ru.ccfit.nsu.bogush.md5bf.client.TaskCreator;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.ALPHABET;
import static ru.ccfit.nsu.bogush.md5bf.MD5BFInfo.PROTOCOL;

public class Server extends Thread implements TaskCreator.TaskCreatorListener {
    private static final int EXIT_FAILURE = 1;
    private static final int TASK_QUEUE_SIZE = 16;
    private static final int TIMED_OUT_TASK_DEQUEUE_SIZE = TASK_QUEUE_SIZE / 2;
    private static final int MAX_SEQUENCE_LENGTH = 12;
    private static final int SUFFIX_LENGTH = 8;
    private static final int REQUIRED_NUMBER_OF_ARGUMENTS = 2;
    private static final int HASH_ARGUMENT_INDEX = 0;
    private static final int PORT_ARGUMENT_INDEX = 1;
    private static final long TASK_TIMEOUT = 1000; // millis
    private static final int ACCEPT_TIMEOUT = 5000; // millis

    private final BlockingQueue<Task> taskQueue = new ArrayBlockingQueue<>(TASK_QUEUE_SIZE);
    private final Deque<Task> timedOutTaskDequeue = new ArrayDeque<>(TIMED_OUT_TASK_DEQUEUE_SIZE);
    private final ServerSocket serverSocket;
    private final TaskCreator taskCreator;
    private final ConcurrentHashMap<UUID, AssignedTask> assignedTasks = new ConcurrentHashMap<>();
    private final int serverPort;
    private boolean tasksFinished = false;
    private boolean secretStringReceived = false;
    private boolean shouldStop = false;

    private Server(String md5hashString, int port) throws IOException {
        super("Server");
        System.err.println("Initialize MD5 Brute-Force Server of the hash " + md5hashString);
        serverPort = port;
        this.serverSocket = new ServerSocket();
        serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
        byte[] md5hash = DatatypeConverter.parseHexBinary(md5hashString);
        taskCreator = new TaskCreator(md5hash, taskQueue, ALPHABET, SUFFIX_LENGTH, MAX_SEQUENCE_LENGTH);
        taskCreator.setTaskCreatorListener(this);
    }

    private static void usage() {
        System.out.print("Usage\n\t");
        System.out.print("java -jar server.jar hash port\n\n");
        System.out.print("Description\n\t");
        System.out.print("A server to send brute-force tasks to client programs that will\n\t");
        System.out.print("brute-force in parallel.\n\n");
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
        try {
            System.err.println("Binding server socket to port " + serverPort);
            serverSocket.bind(new InetSocketAddress(serverPort));
        } catch (IOException e) {
            System.err.println("Couldn't bind server socket");
            return;
        }
        taskCreator.start();
        super.start();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            Socket clientSocket = null;
            try {
                System.err.println("\nListen to incoming connections");
                clientSocket = serverSocket.accept();
            } catch (SocketTimeoutException e) {
                System.err.println("Listen timed out");
            } catch (IOException e) {
                System.err.println("Server: An I/O error occurs when waiting for a connection.");
                continue;
            }

            if (clientSocket != null) {
                handleConnection(clientSocket);
            }

            handleTimedOutTasks();

            if (shouldStop()) {
                break;
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Couldn't close server socket");
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket clientSocket) {
        ObjectInputStream in;
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Couldn't get i/o streams of the client socket");
            try {
                clientSocket.close();
            } catch (IOException e1) {
                System.err.println("FATAL: Couldn't close client socket");
                System.exit(EXIT_FAILURE);
            }
            return;
        }

        System.err.println("Client connecting");

        if (!recognizeClientProtocol(in)) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Couldn't close client socket");
            }
            return;
        }

        UUID uuid;
        try {
            uuid = (UUID) in.readObject();
            System.err.println("Received uuid " + uuid);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Couldn't read uuid");
            return;
        }

        ConnectionRequestType type;
        try {
            type = ConnectionRequestType.forByte(in.readByte());
            System.err.println("Received connection request type " + type);
        } catch (IOException e) {
            System.err.println("Couldn't read connection request type");
            try {
                clientSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }

        if (finishing()) {
            try {
                System.err.println("Close socket and remove assigned tasks of client " + uuid);
                assignedTasks.remove(uuid);
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (assignedTasks.isEmpty()) {
                // all clients either finished or timed out
                shouldStop = true;
            }
            return;
        }

        switch (type) {
            case TASK_REQUEST:
                System.err.println("Received TASK_REQUEST from client " + uuid);
                handleTaskRequest(out, uuid);
                break;
            case TASK_DONE:
                System.err.println("Received TASK_DONE from client " + uuid);
                handleTaskDone(in);
                break;
            default:
                System.err.println("Unknown client connection request type detected");
                return;
        }

        try {
            if (!clientSocket.isClosed()) {
                clientSocket.getOutputStream().flush();
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean recognizeClientProtocol(ObjectInputStream in) {
        try {
            String protocol = (String) in.readObject();
            if (!protocol.equals(PROTOCOL)) {
                System.err.println("The protocol '" + protocol + "' is not recognised");
                return false;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Couldn't read protocol details");
            return false;
        }
        System.err.println("Protocol details recognised");
        return true;
    }

    private void handleTaskRequest(ObjectOutputStream out, UUID uuid) {
        Task task;
        try {
            if (timedOutTaskDequeue.isEmpty()) {
                task = taskQueue.take();
            } else {
                task = timedOutTaskDequeue.remove();
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted while taking task from queue");
            return;
        }

        assignedTasks.put(uuid, new AssignedTask(task, System.currentTimeMillis() + TASK_TIMEOUT));

        try {
//            out.writeBoolean(true); // tasks are available
            out.writeObject(task);
        } catch (IOException e) {
            System.err.println("Couldn't write task");
        }
    }

    private void handleTaskDone(ObjectInputStream in) {
        String secretString;
        try {
            secretString = (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Couldn't read secret string");
            return;
        }
        System.out.println("secret string is: \"" + secretString + '"');
        finish();
    }

    private void handleTimedOutTasks() {
        assignedTasks.forEach((uuid, assignedTask) -> {
            if (System.currentTimeMillis() > assignedTask.timeLimit) {
                assignedTasks.remove(uuid);
                System.err.println("Client " + uuid + " task timed out");
                timedOutTaskDequeue.push(assignedTask.task);
            }
        });
    }

    private void finish() {
        taskCreator.interrupt();
        taskQueue.clear();
        timedOutTaskDequeue.clear();
        tasksFinished = true;
        secretStringReceived = true;
    }

    private boolean finishing() {
        return secretStringReceived || tasksFinished && taskQueue.isEmpty() && timedOutTaskDequeue.isEmpty();
    }

    private boolean shouldStop() {
        return shouldStop || finishing() && assignedTasks.isEmpty();
    }

    @Override
    public void interrupt() {
        if (taskCreator.isAlive()) {
            taskCreator.interrupt();
        }
        super.interrupt();
    }

    @Override
    public void tasksFinished() {
        System.err.println("Tasks finished");
        tasksFinished = true;
    }

    private static class AssignedTask {
        private Task task;
        private long timeLimit;

        private AssignedTask(Task task, long timeLimit) {
            this.task = task;
            this.timeLimit = timeLimit;
        }
    }
}
