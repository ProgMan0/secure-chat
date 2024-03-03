import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class WebsocketClient {
    public static void main(String[] args) {
        try {
                Socket socket = new Socket("2.tcp.eu.ngrok.io", 16543);

                System.out.println("Connected to " + socket.getInetAddress());

                Thread thread = new Thread(send(socket));
                Thread thread1 = new Thread(receive(socket));

                thread.start();
                thread1.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Runnable send(Socket socket) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

                    while (true) {
                        Scanner scanner = new Scanner(System.in);

                        printWriter.println(scanner.nextLine());
                        printWriter.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static Runnable receive(Socket socket) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    while (true) {
                        String text = bufferedReader.readLine();
                        if (!text.equals("")) {
                            System.out.println(text);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
