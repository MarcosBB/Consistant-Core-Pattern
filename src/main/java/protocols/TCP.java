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
                        StringBuilder messageBuilder = new StringBuilder();
                        String line;
                        while ((line = input.readLine()) != null) {
                            messageBuilder.append(line).append("\n");
                            if (line.isEmpty())
                                break;
                        }
                        String message = messageBuilder.toString().trim();
                        boolean processed = processPayload.apply(message);

                        // Send a response back to the client
                        PrintWriter output = new PrintWriter(connection.getOutputStream());
                        if (processed) {
                            output.println(successResponseMessage);
                        } else {
                            output.println(errorResponseMessage);
                        }
                        output.println();
                        output.flush();
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
    public boolean send(int port, String message, String expectedResponseMessage) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", port), 1000);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            out.println(message);
            out.println();
            out.flush();

            // Wait for a response
            socket.setSoTimeout(1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                responseBuilder.append(line).append("\n");
                if (line.isEmpty())
                    break;
            }
            String response = responseBuilder.toString().trim();
            response = MessageUtils.extract(response, getName());
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
}
