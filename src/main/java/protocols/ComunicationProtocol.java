package protocols;

import java.util.function.Function;

public interface ComunicationProtocol {
    void listen(int port, Function<String, Boolean> processPayload);

    boolean send(int port, String message);

}
