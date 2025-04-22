package protocols;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class TCP implements ComunicationProtocol {

    @Override
    public void listen(int port, Function<String, Boolean> processPayload) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            ServerSocket socket = new ServerSocket(port, 1000);
            executor.execute(() -> {
                try {
                    while (true) {
                        Socket connection = socket.accept();
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String message = input.readLine();
                        boolean processed = processPayload.apply(message);

                        // Send a response back to the client
                        PrintWriter output = new PrintWriter(connection.getOutputStream(), true);
                        if (processed) {
                            output.println("Process done successfully");
                        } else {
                            output.println("Process failed");
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean send(int port, String message) {
        try (Socket socket = new Socket("localhost", port)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);

            // Wait for a response
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine();
            return response.equals("Process done successfully");

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("Response timed out.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
