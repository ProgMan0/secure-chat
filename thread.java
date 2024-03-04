package ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ClientHandlerThread extends Thread
{
    private String clientUsername, encryptKey;

    private Socket clientSocket;
    private ClientHandlerThread[] threads;

    private PrintWriter pr;
    private BufferedReader bufferedReader;

    public ClientHandlerThread(Socket clientSocket, ClientHandlerThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
    }

    @Override
    public void run() {
        ClientHandlerThread[] threads = this.threads;

        try {
            pr = new PrintWriter(clientSocket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            pr.println("Write name: ");
            String name;

            while (true) {
                name = bufferedReader.readLine().trim();
                if (name.indexOf("@") == -1) {
                    break;
                } else {
                    pr.println("Delete @ from name");
                }
            }

            clientUsername = name;
            pr.println("Welcome " + clientUsername + " to our chat!");

            synchronized (this) {
                for (int i = 0; i < threads.length; i++) {
                    if (threads[i] != this && threads[i] != null && threads[i].clientUsername != null) {
                        threads[i].pr.println(clientUsername + " connected to chat!");
                    }
                }
            }

            while (true) {
                String line = bufferedReader.readLine();
                if (line == null || line.startsWith("/quit")) {
                    break;
                }

                if (line.startsWith("@")) {
                    String[] args = line.split("\\s", 2);

                    if (args.length >= 1) {
                        synchronized (this) {
                            for (int i = 0; i < threads.length; i++) {
                                if (threads[i] != null && threads[i].clientUsername != null) {
                                    if (threads[i].clientUsername.equals(args[0].substring(1))) {
                                        threads[i].pr.println("(private) " + args[1].replaceAll("[^a-z0-9 ]", ""));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    synchronized (this) {
                        for (int i = 0; i < threads.length; i++) {
                            if (threads[i] != null) {
                                threads[i].pr.println("[" + getTime() + "] " + clientUsername + " > "
                                        + line.replaceAll("[^a-zA-Z0-9 ]", ""));
                            }
                        }
                    }
                }
            }

            synchronized (this) {
                removeClient();
            }

        } catch (IOException e) {
            removeClient();
        }
    }

    private String getTime() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return currentTime.format(formatter);
    }


    private void removeClient() {
        synchronized (this) {
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] == this) {
                    threads[i] = null;
                }
                if (threads[i] != null) {
                    threads[i].pr.println("[" + getTime() + "] " + clientUsername + " has left the chat.");
                }
            }
        }
        try {
            pr.close();
            bufferedReader.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
