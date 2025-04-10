package protocols;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class TCP implements ComunicationProtocol {

    @Override
    public void listen(int port, Consumer<String> processPayload) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            ServerSocket socket = new ServerSocket(port, 1000);
            executor.execute(() -> {
                try {
                    while (true) {
                        Socket conexao = socket.accept();
                        BufferedReader input = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
                        String message = input.readLine();
                        processPayload.accept(message);
                        // Send a response back to the client
                        PrintWriter output = new PrintWriter(conexao.getOutputStream(), true);
                        output.println("Process done successfully");
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
    public void send(int port, String message) {
        try (Socket socket = new Socket("localhost", port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
