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
        try (
                ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                ServerSocket server = new ServerSocket(port, 1000)) {

            while (true) {
                Socket conexao = server.accept();
                BufferedReader input = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
                String message = input.readLine();
                executor.execute(() -> processPayload.accept(message));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(String message, int port) {
        try (Socket socket = new Socket("localhost", port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(message);
            System.out.println("TCP message sent to port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
