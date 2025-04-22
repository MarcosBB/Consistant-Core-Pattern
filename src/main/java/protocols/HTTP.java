package protocols;

import java.util.function.Function;

public class HTTP implements ComunicationProtocol {
    private TCP protocol;

    public HTTP() {
        this.protocol = new TCP();
    }

    @Override
    public void listen(
            int port,
            Function<String, Boolean> processPayload,
            String successResponseMessage,
            String errorResponseMessage) {
        protocol.listen(
                port,
                processPayload,
                formatSuccessResponse(successResponseMessage),
                formatErrorReponse(errorResponseMessage));

    }

    @Override
    public boolean send(int port, String message, String expectedResponseMessage) {
        return protocol.send(
                port,
                formatMessage("POST", port, message),
                formatSuccessResponse(expectedResponseMessage));

    }

    @Override
    public String getName() {
        return "HTTP";
    }

    private String formatMessage(String method, int port, String message) {
        return method.toUpperCase() + " / HTTP/1.1\r\n" +
                "Host: localhost:" + port + "\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "Content-Type: application/x-www-form-urlencoded\r\n" +
                "\r\n" +
                message;
    }

    private String formatErrorReponse(String message) {
        return "HTTP/1.1 500 Internal Server Error\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" +
                message;
    }

    private String formatSuccessResponse(String message) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" +
                message;
    }

}
