package M4.Part2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

public class Server {
    private int port = 3000;
    private List<ServerThread> clients = new ArrayList<>();

    public synchronized void registerClient(ServerThread client) {
        clients.add(client);
    }

    public synchronized void sendPM(int targetId, String message, ServerThread sender) {
        String fullMessage = "PM from " + sender.getDisplayName() + ": " + message;
        sender.send(fullMessage); // also send to the sender

        for (ServerThread client : clients) {
            if (client.getId() == targetId) {
                client.send(fullMessage);
                return;
            }
        }
        sender.send("Server: Could not find client with ID " + targetId);
    }

    private void start(int port) {
        this.port = port;
        System.out.println("Listening on port " + this.port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                ServerThread thread = new ServerThread(client, this);
                thread.start();
                System.out.println("Client connected and handler started");
            }
        } catch (IOException e) {
            System.out.println("Exception from start()");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Server Starting");
        Server server = new Server();
        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // default to 3000
        }
        server.start(port);
        System.out.println("Server Stopped");
    }
}

