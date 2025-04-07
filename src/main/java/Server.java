import protocols.ComunicationProtocol;
import utils.ProtocolUtils;

public class Server {

    public static void main(String[] args) {
        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);

        System.out.println("Listening for messages...");
        while (true) {
            protocol.listen(8080, message -> {
                System.out.println("Received message: " + message);
            });
        }
    }

}
