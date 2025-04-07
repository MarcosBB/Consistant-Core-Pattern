import protocols.ComunicationProtocol;
import utils.ProtocolUtils;

public class Client {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please specify a protocol (UDP, TCP, HTTP) and a port.");
            return;
        }

        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);
        int port = Integer.parseInt(args[1]);

        protocol.send(port, "Hello, Server!");
    }
}
