import protocols.ComunicationProtocol;
import utils.ProtocolUtils;

public class Client {
    public static void main(String[] args) {

        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);
        protocol.send("Hello, Server!", 8080);
        System.out.println("Message sent to server.");
    }
}
