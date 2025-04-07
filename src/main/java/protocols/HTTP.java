package protocols;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.net.ServerSocket;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class HTTP implements ComunicationProtocol {

    @Override
    public void listen(int port, Consumer<String> processPayload) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    try {
                        byte[] buffer = new byte[1024];
                        int bytesRead = clientSocket.getInputStream().read(buffer);
                        String request = new String(buffer, 0, bytesRead).trim();

                        if (request.contains("\r\n\r\n")) {
                            String body = getTextBody(request);
                            processPayload.accept(body);
                            respond(clientSocket, 200, getStatusMessage(200));
                        } else {
                            respond(clientSocket, 400, getStatusMessage(400));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        respond(clientSocket, 500, getStatusMessage(500));
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(int port, String message) {
        try (Socket socket = new Socket("localhost", port);
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true)) {

            StringBuilder httpMessage = new StringBuilder();
            httpMessage.append("POST / HTTP/1.1\r\n");
            httpMessage.append("Host: localhost:").append(port).append("\r\n");
            httpMessage.append("Content-Type: text/plain\r\n");
            httpMessage.append("Content-Length: ").append(message.length()).append("\r\n");
            httpMessage.append("\r\n");
            httpMessage.append(message);

            writer.write(httpMessage.toString());
            writer.flush();
            System.out.println("HTTP message sent to port " + port);

            // Wait for server response
            byte[] buffer = new byte[1024];
            int bytesRead = socket.getInputStream().read(buffer);
            if (bytesRead != -1) {
                String response = new String(buffer, 0, bytesRead).trim();
                String responseBody = getTextBody(response);
                String statusCode = getStatusCode(response);
                System.out.println("Response: " + statusCode + " - " + responseBody);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void respond(Socket clientSocket, int statusCode, String message) {
        try (OutputStream output = clientSocket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true)) {

            StringBuilder httpResponse = new StringBuilder();
            httpResponse.append("HTTP/1.1 ").append(statusCode).append(" ")
                    .append(getStatusMessage(statusCode)).append("\r\n");
            httpResponse.append("Content-Type: text/plain\r\n");
            httpResponse.append("Content-Length: ").append(message.length())
                    .append("\r\n");
            httpResponse.append("\r\n");
            httpResponse.append(message);

            writer.write(httpResponse.toString());
            writer.flush();
            System.out.println("HTTP response sent with status code " + statusCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 400:
                return "Bad Request";
            case 404:
                return "Not Found";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown Status";
        }
    }

    private String getTextBody(String request) {
        int bodyIndex = request.indexOf("\r\n\r\n");
        if (bodyIndex != -1) {
            return request.substring(bodyIndex + 4).trim();
        }
        return "";
    }

    private String getStatusCode(String response) {
        String[] lines = response.split("\r\n");
        if (lines.length > 0) {
            return lines[0].split(" ")[1];
        }
        return "";
    }

}
