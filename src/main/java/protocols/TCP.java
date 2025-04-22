package protocols;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import utils.MessageUtils;

public class TCP implements ComunicationProtocol {

    @Override
    public void listen(
            int port,
            Function<String, Boolean> processPayload,
            String successResponseMessage,
            String errorResponseMessage) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            ServerSocket socket = new ServerSocket(port, 1000);
            executor.execute(() -> {
                try {
                    while (true) {
                        Socket connection = socket.accept();
                        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String message = buildMessage(input);
                        boolean processed = processPayload.apply(message);

                        // Send a response back to the client
                        PrintWriter output = new PrintWriter(connection.getOutputStream());
                        if (processed) {
                            output.println(successResponseMessage);
                        } else {
                            output.println(errorResponseMessage);
                        }
                        output.println("--END--");
                        output.flush();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean send(int port, String message, String expectedResponseMessage) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", port), 1000);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(message);
            out.println("--END--");
            out.flush();

            // Wait for a response
            socket.setSoTimeout(1000);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = MessageUtils.extract(buildMessage(input), getName());
            return response.equals(expectedResponseMessage);

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("Response timed out.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getName() {
        return "TCP";
    }

    private String buildMessage(BufferedReader input) {
        StringBuilder messageBuilder = new StringBuilder();
        String line;
        try {
            while ((line = input.readLine()) != null) {
                if (line.equals("--END--")) {
                    return messageBuilder.toString().trim();
                }
                messageBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
