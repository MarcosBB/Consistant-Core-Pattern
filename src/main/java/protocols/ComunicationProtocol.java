package protocols;

import java.util.function.Function;

public interface ComunicationProtocol {
    void listen(
            int port,
            Function<String, Boolean> processPayload,
            String successResponseMessage,
            String errorResponseMessage);

    boolean send(int port, String message, String expectedResponseMessage);

}
