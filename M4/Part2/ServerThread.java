package M4.Part2;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Server server;

    public ServerThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            server.registerClient(this);
            System.out.println("Client " + getId() + " registered");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client (" + getId() + "): " + inputLine);

                if (inputLine.startsWith("/pm ")) {
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length < 3) {
                        send("Server: Invalid /pm format. Use /pm <id> <message>");
                        continue;
                    }

                    try {
                        int targetId = Integer.parseInt(parts[1]);
                        String msg = parts[2];
                        server.sendPM(targetId, msg, this);
                    } catch (NumberFormatException e) {
                        send("Server: Invalid target ID");
                    }
                } else {
                    send("Server: Unknown command");
                }
            }
        } catch (IOException e) {
            System.out.println("Client " + getId() + " disconnected.");
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String getDisplayName() {
        return "Client-" + getId();
    }
    
}