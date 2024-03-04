package ws;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebsocketServer {
    private static ServerSocket serverSocket = null;
    private static Socket clientSocket = null;

    private static int maxClients = 10;
    private static ClientHandlerThread[] threads = new ClientHandlerThread[maxClients];

    public static void main(String[] args) {
        int port = 2222;

        if (args.length < 1) {
            System.out.println("Usage: java WebsocketServer.java <port>\n" +
                    "current port => " + port);
        } else {
            port = Integer.parseInt(args[0]);
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                clientSocket = serverSocket.accept();
                int i = 0;

                for (i = 0; i < maxClients; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClientHandlerThread(clientSocket, threads)).start();
                        break;
                    }
                }

                if (i == maxClients) {
                    PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                    printWriter.println("Server is full");

                    printWriter.close();
                    clientSocket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
