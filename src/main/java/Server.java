import protocols.ComunicationProtocol;
import protocols.HeartBeat;
import utils.ProtocolUtils;

public class Server {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Please specify a protocol (UDP, TCP, HTTP), server port and gateway port.");
            return;
        }

        ComunicationProtocol protocol = ProtocolUtils.setProtocol(args[0]);
        int port = Integer.parseInt(args[1]);
        int gatewayPort = Integer.parseInt(args[2]);

        protocol.listen(port, message -> {
            System.out.println("Received message: " + message);
            return true;
        });

        HeartBeat heartBeat = new HeartBeat(protocol);
        heartBeat.startSendingHeartBeats(port, gatewayPort);

    }
}
