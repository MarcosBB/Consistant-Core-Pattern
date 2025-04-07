package protocols;

import java.util.function.Consumer;

public interface ComunicationProtocol {
    void listen(int port, Consumer<String> processPayload);

    void send(int port, String message);

}
